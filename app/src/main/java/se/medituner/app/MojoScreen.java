package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.Gravity.CENTER;

public class MojoScreen extends AppCompatActivity {

    public static final int MS_SNOOZE_DELAY = 60000;                // Snooze time after answering 'no'
    public static final int MS_FIRST_POPUP_DELAY = 1000;            // Delay between the start of the app and the first question popup appearing
    public static final int MS_POPUP_DELAY = 1500;                  // Delay between stacked popups appearing
    public static final int MS_REWARD_STREAK_SHOW_DURATION = 1600;  // Duration of the streak popup appearing animation
    public static final int MS_REWARD_STREAK_HIDE_DURATION = 1200;  // Duration of the streak popup disappearing animation
    public static final int MS_REWARD_STREAK_HIDE_DELAY = 1800;     // Delay between the streak popup appearing and disappearing.
    public static final int MS_REWARD_STREAK_SHOW_DELAY = 800;      // A delay before the streak increasing and the reward popup appearing. Should not be 0 for technical reasons

    public static final String SCHEDULE_FILENAME = "schedule";
    public static final String STREAK_FILENAME= "streak";

    private IClock time = new SystemClock();

    private Popup questionPopup, streakPopup;
    private Streak streak;
    private TextView streakView, questionTextView, rewardStreakTextView;
    private boolean animationPlayed = false;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;
    private ImageView smilingBounceMojo, smilingWaveMojo, frowningMojo, grinningBounceMojo, questionImageView;
    private View streakPopupView;
    private Persistence persistence;
    private Timer scheduler = new Timer(true);

    private Schedule schedule;
    private Queue<Medication> medicationQueue;

    /**
     * The first thing to be called on app startup.
     * Most of the initialization happens here.
     *
     * @param savedInstanceState Android caching
     * @author Grigory Glukhov, Aleksandra Soltan, Sasa Lekic, Julia Danek, Agnes Petajavaara, Vendela Vlk
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        persistence = new Persistence(this);

        // Set up popups
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);
        questionImageView = questionPopup.getPopupView().findViewById(R.id.medication_image);
        questionTextView = questionPopup.getPopupView().findViewById(R.id.text_medication_question);

        streakPopup = new Popup(this, R.layout.streak_popup);
        streakPopupView = streakPopup.getPopupView();
        rewardStreakTextView = streakPopupView.findViewById(R.id.text_reward_streak);

        // Load sounds
        Sounds.getInstance().loadSounds(this);

        // Set up streaks
        streakView = findViewById(R.id.streak_text);
        try {
            streak = (Streak) persistence.loadObject(STREAK_FILENAME);
        } catch (IOException e) {
            System.err.println("Could not load streak, resetting it.");
            streak = new Streak();
            try {
                persistence.saveObject(streak, STREAK_FILENAME);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            finish();
        } catch (ClassCastException e) {
            e.printStackTrace();
            System.err.println("Resetting streak");
            streak = new Streak();
            try {
                persistence.saveObject(streak, STREAK_FILENAME);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        streak.setListener(new StreakListener());
        streakView.setText(getResources().getString(R.string.streak, streak.getValue()));

        // Set up animations
        accelerateInterpolator = new AccelerateInterpolator();
        bounceInterpolator = new BounceInterpolator();
        smilingBounceMojo = (ImageView) findViewById(R.id.smilingBounceMojo);
        smilingWaveMojo = (ImageView) findViewById(R.id.smilingWaveMojo);
        grinningBounceMojo = (ImageView) findViewById(R.id.grinningBounceMojo);
        frowningMojo = (ImageView) findViewById(R.id.frowningMojo);

        smilingBounceMojo.bringToFront();
        smilingWaveMojo.bringToFront();
        frowningMojo.bringToFront();
        grinningBounceMojo.bringToFront();

        // Set up schedule
        initializeSchedule();
        medicationQueue = schedule.getActiveQueue();

        // Check for medication
        scheduler.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    checkMedication();
                }
            }, MS_FIRST_POPUP_DELAY);
    }

    /**
     * Updates the schedule, validates the medication queue and then shows question popup if necessary.
     *
     * @author Grigory Glukhov
     */
    public void checkMedication() {
        schedule.validateQueue(true);
        medicationQueue = schedule.getActiveQueue();

        if (medicationQueue.isEmpty()) {
            try {
                persistence.saveObject(schedule, SCHEDULE_FILENAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            scheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkMedication();
                }
            }, Schedule.getBeginningOfNextPeriod(time));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showQuestionPopup();
                }
            });
        }

        schedule.validateQueue(true);
    }

    /**
     * Initialize the medication schedule.
     *
     * @author Grigory Glukhov, Aleksandra Soltan
     */
    private void initializeSchedule() {
        try {
            System.out.println("Attempting to load schedule.");
            schedule = (Schedule) persistence.loadObject(SCHEDULE_FILENAME);
        } catch (IOException e) {
            System.out.println("Schedule not found. Creating new one.");
            e.printStackTrace();
            schedule = Schedule.generate(time);
            try {
                persistence.saveObject(schedule, SCHEDULE_FILENAME);
            } catch (IOException e1) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            finish();
        }

        schedule.connectStreak(streak);
        schedule.validateQueue(true);
    }

    /**
     * Called when 'generate schedule' button is pressed.
     *
     * Generates a new schedule, saves it, updates medication queue and finally shows the medication popup.
     *
     * @param view Android button view that was pressed.
     */
    public void onButtonGenerateSchedule(View view) {
        schedule = Schedule.generate(time);
        try {
            persistence.saveObject(schedule, SCHEDULE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        schedule.connectStreak(streak);
        checkMedication();
    }

    /**
     * Called when 'play' button is pressed.
     *
     * Switches to the game activity.
     *
     * @param view Android button view that was pressed.
     */
    public void onButtonPlay(View view) {
        Intent intent = new Intent(this, OpenGLES20Activity.class);
        startActivity(intent);
    }

    /**
     * Called when 'reset queue' button is pressed.
     *
     * Resets the current queue.
     *
     * @param view Android button view that was pressed.
     */
    public void onButtonResetQueue(View view) {
        schedule.resetQueue();
        try {
            persistence.saveObject(schedule, SCHEDULE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkMedication();
    }


    /**
     * Show a popup and animated Mojo reaction.
     *
     * @author Grigory Glukhov, Aleksandra Soltan, Sasa Lekic
     */
    public void showQuestionPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);
        // Set the dynamic image and name
        setPopupMedication(medicationQueue.element());
        // Show popup
        questionPopup.showPopupWindow(currentScreen);
    }


    /**
     * Show reward streak popup.
     *
     * @author Sasa Lekic
     */
    public void showStreakPopup() {
        View currentScreen = findViewById(R.id.activity_mojo_screen);
        rewardStreakTextView.setText(getResources().getString(R.string.streak_popup, streak.getValue()));

        Sounds.getInstance().playSound(Sounds.Sound.S_STAR1);
        streakPopupView.setScaleX(0.0f);
        streakPopupView.setScaleY(0.0f);
        streakPopupView.setRotation(-180.0f);
        streakPopup.showPopupWindow(currentScreen, CENTER, 0, -240);
        streakPopupView.animate()
                .setDuration(MS_REWARD_STREAK_SHOW_DURATION)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .rotation(0.0f)
                .setListener(null);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Sounds.getInstance().playSound(Sounds.Sound.S_STAR2);
                streakPopupView.animate()
                        .setDuration(MS_REWARD_STREAK_HIDE_DURATION)
                        .scaleX(0.0f)
                        .scaleY(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                streakPopup.dismissPopupWindow();
                            }
                        });
            }
        }, MS_REWARD_STREAK_HIDE_DELAY + MS_REWARD_STREAK_SHOW_DURATION);

        Sounds.getInstance().playSound(Sounds.Sound.S_JUMP);

        frowningMojo.setVisibility(View.INVISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);
        smilingBounceMojo.setVisibility(View.INVISIBLE);
        grinningBounceMojo.setVisibility(View.VISIBLE);

        ViewPropertyAnimator viewPropertyAnimator = grinningBounceMojo.animate()
                .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Mojo falls and bounces
                        grinningBounceMojo.animate()
                                .translationY(0)
                                .setInterpolator(bounceInterpolator).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //Smiling, waving Mojo visible
                                smilingWaveMojo.setVisibility(View.VISIBLE);
                                smilingBounceMojo.setVisibility(View.INVISIBLE);
                                grinningBounceMojo.setVisibility(View.INVISIBLE);

                                smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                
                                // Get the background, which has been compiled to an AnimationDrawable object.
                                AnimationDrawable waveAnimation = (AnimationDrawable) smilingWaveMojo.getBackground();
                                // Start the animation, Mojo waves
                                waveAnimation.start();

                                Sounds.getInstance().playSound(Sounds.Sound.S_HAPPY);
                            }
                        });
                    }
                });

    }

    /**
     * Set the name and the image for a given medication in the question popup.
     *
     * @param medication The medication to show.
     * @author Grigory Glukhov, Julia Danek
     */
    public void setPopupMedication(Medication medication) {
        questionImageView.setImageResource(Medication.getImageId(getResources(), getPackageName(), medication));
        questionTextView.setText(getResources().getString(R.string.popup_question,
                Medication.getName(getResources(), getPackageName(), medication)));
    }

    /**
     * Called when user answers 'yes' on the question popup.
     * Initiates happy Mojo animations and sounds.
     *
     * @param view Android button that was pressed.
     */
    public void onButtonYes(View view) {
        // Play jump sound
        Sounds.getInstance().playSound(Sounds.Sound.S_JUMP);

        // Hide the popup
        questionPopup.dismissPopupWindow();

        //Smiling, jumping Mojo visible
        frowningMojo.setVisibility(View.INVISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);

        if (streakFunction()) {
            smilingBounceMojo.setVisibility(View.INVISIBLE);
            grinningBounceMojo.setVisibility(View.VISIBLE);
        } else {
            smilingBounceMojo.setVisibility(View.VISIBLE);
            grinningBounceMojo.setVisibility(View.INVISIBLE);
            ViewPropertyAnimator viewPropertyAnimator = smilingBounceMojo.animate()
                    .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //Mojo falls and bounces
                            smilingBounceMojo.animate()
                                    .translationY(0)
                                    .setInterpolator(bounceInterpolator).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    //Smiling, waving Mojo visible
                                    smilingWaveMojo.setVisibility(View.VISIBLE);
                                    smilingBounceMojo.setVisibility(View.INVISIBLE);
                                    grinningBounceMojo.setVisibility(View.INVISIBLE);

                                    smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                    // Get the background, which has been compiled to an AnimationDrawable object.
                                    AnimationDrawable waveAnimation = (AnimationDrawable) smilingWaveMojo.getBackground();
                                    // Start the animation, Mojo waves
                                    waveAnimation.start();

                                    Sounds.getInstance().playSound(Sounds.Sound.S_HAPPY);
                                }
                            });
                        }
                    });

        }

        medicationQueue.remove();
        try {
            persistence.saveObject(schedule, SCHEDULE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                checkMedication();
            }
        }, MS_POPUP_DELAY);
    }


    /**
     * Called when the user presses 'no' on the popup question.
     * Dismisses the window, sets off sad Mojo animations and sounds.
     *
     * @param view Android button that was pressed.
     */
    public void onButtonNo(View view) {
        // Hide the popup
        questionPopup.dismissPopupWindow();

        //Frowning Mojo visible
        frowningMojo.setVisibility(View.VISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);
        smilingBounceMojo.setVisibility(View.INVISIBLE);
        grinningBounceMojo.setVisibility(View.INVISIBLE);

        frowningMojo.setBackgroundResource(R.drawable.frown_animation);
        AnimationDrawable frownAnimation = (AnimationDrawable) frowningMojo.getBackground();

        if (animationPlayed) {
            frownAnimation.stop();
        }

        frownAnimation.start();

        // Play sad sound
        Sounds.getInstance().playSound(Sounds.Sound.S_SAD);

        animationPlayed = true;

        snoozePopup(MS_SNOOZE_DELAY);
    }


    /**
     * Determine if its time to show the streak popup.
     *
     * @return True if the reward popup should be shown.
     * @author Aleksandra Soltan
     */
    public boolean streakFunction() {
        if (((streak.getValue() == 3) || (streak.getValue() % 6 == 0)) && streak.getValue() != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show the popup after set amount of milliseconds.
     *
     * @param delay The amount of milliseconds to snooze for.
     * @author Aleksandra Soltans
     */
    private void snoozePopup(int delay) {
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                checkMedication();
            }
        }, delay);
    }

    private class StreakListener implements Streak.ChangeListener {

        @Override
        public void onStreakChanged(final int newStreak) {
            try {
                persistence.saveObject(streak, STREAK_FILENAME);
            } catch (IOException e) {
                e.printStackTrace();
            }

            scheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            streakView.setText(getResources().getString(R.string.streak, newStreak));
                            if (streakFunction())
                                showStreakPopup();
                        }
                    });
                }
            }, MS_REWARD_STREAK_SHOW_DELAY);
        }
    }
}