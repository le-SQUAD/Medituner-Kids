package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
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

    public static final int MS_SNOOZE_DELAY = 5000;                 // Snooze time after answering 'no'
    public static final int MS_FIRST_POPUP_DELAY = 1000;            // Delay between the start of the app and the first question popup appearing
    public static final int MS_POPUP_DELAY = 1500;                  // Delay between stacked popups appearing
    public static final int MS_REWARD_STREAK_SHOW_DURATION = 1600;  // Duration of the streak popup appearing animation
    public static final int MS_REWARD_STREAK_HIDE_DURATION = 1200;  // Duration of the streak popup disappearing animation
    public static final int MS_REWARD_STREAK_HIDE_DELAY = 1800;     // Delay between the streak popup appearing and disappearing.

    public static final String SCHEDULE_FILENAME = "schedule";

    private IClock time = new SystemClock();

    private Popup questionPopup, streakPopup;
    private int streak = 0;
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
        resetStreak();

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
        // TODO: update the actual schedule
        schedule.validateQueue();
        medicationQueue = schedule.getActiveQueue();

        if (medicationQueue.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    showStreakPopup();
                }
            });

            scheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkMedication();
                }
            }, Schedule.getBeginningOfNextPeriod(time).getTime());
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showQuestionPopup();
                }
            });
        }
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
            // Set up Schedule
            schedule = new Schedule(time);

            schedule.addMedToMorningPool(Medication.AEROBEC);
            schedule.addMedToMorningPool(Medication.AIROMIR);
            schedule.addMedToMorningPool(Medication.ALVESCO);

            schedule.addMedToLunchPool(Medication.BRICANYLTURBOHALER);
            schedule.addMedToLunchPool(Medication.SALMETEROLFLUTICASONECIPLA);
            schedule.addMedToLunchPool(Medication.SERETIDEDISKUSLILA);

            schedule.addMedToEveningPool(Medication.BUFOMIXMEDIUM);
            schedule.addMedToEveningPool(Medication.EYEDROP);
            schedule.addMedToEveningPool(Medication.SERETIDEDISKUSLILA);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            finish();
        }

        schedule.validateQueue();
    }

    /**
     * Temporary button that shows the popup immediately.
     *
     * @param view Android button that was pressed.
     * @author Grigory Glukhov
     */
    public void onButtonShowPopupClick(View view) {
        schedule.validateQueue();
        medicationQueue = schedule.getActiveQueue();
        if (medicationQueue.isEmpty()) {
            showStreakPopup();
        } else {
            showQuestionPopup();
        }
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
        rewardStreakTextView.setText(getResources().getString(R.string.streak_popup, streak));

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

                                //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation_grinning);
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
        incrementStreak();
        //final Integer resid;
        //final ImageView mojoBounceExpression;

        // Play jump sound
        Sounds.getInstance().playSound(Sounds.Sound.S_JUMP);

        // Hide the popup
        questionPopup.dismissPopupWindow();

        //Smiling, jumping Mojo visible
        frowningMojo.setVisibility(View.INVISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);
        //smilingBounceMojo.setVisibility(View.VISIBLE);

        if (streakFunction()) {
            smilingBounceMojo.setVisibility(View.INVISIBLE);
            grinningBounceMojo.setVisibility(View.VISIBLE);
            //resid = R.drawable.arm_animation_grinning;
            //mojoBounceExpression = grinningBounceMojo;
            showStreakPopup();
        }
        else{
            smilingBounceMojo.setVisibility(View.VISIBLE);
            grinningBounceMojo.setVisibility(View.INVISIBLE);
            //mojoBounceExpression = smilingBounceMojo;
            //resid = R.drawable.arm_animation;
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

                                    //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                    //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
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

        //Mojo jumps
        /*ViewPropertyAnimator viewPropertyAnimator = mojoBounceExpression.animate()
                .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Mojo falls and bounces
                       mojoBounceExpression.animate()
                                .translationY(0)
                                .setInterpolator(bounceInterpolator).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //Smiling, waving Mojo visible
                                smilingWaveMojo.setVisibility(View.VISIBLE);
                                smilingBounceMojo.setVisibility(View.INVISIBLE);
                                grinningBounceMojo.setVisibility(View.INVISIBLE);

                                //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                //smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                smilingWaveMojo.setBackgroundResource(resid);
                                // Get the background, which has been compiled to an AnimationDrawable object.
                                AnimationDrawable waveAnimation = (AnimationDrawable) smilingWaveMojo.getBackground();
                                // Start the animation, Mojo waves
                                waveAnimation.start();

                                Sounds.getInstance().playSound(Sounds.Sound.S_HAPPY);
                            }
                        });
                    }
                });*/

        medicationQueue.remove();
        try {
            persistence.saveObject(schedule, SCHEDULE_FILENAME);
        } catch (IOException e) {
            System.err.println(e);
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
        resetStreak();

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
        if (((streak == 3) || (streak % 6 == 0)) && streak != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Increments the streak counter, updates corresponding text and shows reward popup if required.
     *
     * @autor Sasa Lekic, Julia Danek
     */
    private void incrementStreak() {
        streakView.setText(getResources().getString(R.string.streak, ++streak));
        if (streakFunction())
            showStreakPopup();
    }

    /**
     * Resets the streak counter to 0 and updates corresponding text.
     *
     * @autor Sasa Lekic, Julia Danek
     */
    private void resetStreak() {
        streak = 0;
        streakView.setText(getResources().getString(R.string.streak, streak));
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
}