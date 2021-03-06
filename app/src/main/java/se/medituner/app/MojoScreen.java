package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import se.medituner.app.game.GameScreen;

import static android.view.Gravity.CENTER;

public class MojoScreen extends AppCompatActivity {

    public static final int MS_SNOOZE_DELAY = 60000;                // Snooze time after answering 'no'
    public static final int MS_FIRST_POPUP_DELAY = 2400;            // Delay between the start of the app and the first question popup appearing
    public static final int MS_POPUP_DELAY = 1500;                  // Delay between stacked popups appearing
    public static final int MS_REWARD_STREAK_SHOW_DURATION = 1600;  // Duration of the streak popup appearing animation
    public static final int MS_REWARD_STREAK_HIDE_DURATION = 1200;  // Duration of the streak popup disappearing animation
    public static final int MS_REWARD_STREAK_HIDE_DELAY = 1800;     // Delay between the streak popup appearing and disappearing.
    public static final int MS_REWARD_STREAK_SHOW_DELAY = 800;      // A delay before the streak increasing and the reward popup appearing. Should not be 0 for technical reasons

    public static final int MS_OBSTACLE_POPUP_DELAY = 2400;
    public static final int MS_BUTTON_DELAY = 200;
    public static final int MS_MOVE_INSTRUCTION_DELAY = 1200;
    public static final int MS_LUNG_DELAY = 2400;

    public static final String SCHEDULE_FILENAME = "schedule";
    public static final String STREAK_FILENAME= "streak";

    private static final float QUESTION_POPUP_OFFSET_Y_RELATIVE = -0.1f;

    private static MojoScreen instance;

    private IClock time = new SystemClock();

    private Popup questionPopup, streakPopup, gameInstructionPopup;
    private Streak streak;
    private TextView streakView, questionTextView, rewardStreakTextView, gameInstructionTextView, congratulationTextView;
    private boolean frownAnimationPlayed = false;
    private boolean smileWaveAnimationPlayed = false;
    private boolean grinWaveAnimationPlayed = false;
    private boolean gameInstructionsPlayed = false;
    private AnimationDrawable waveAnimation;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;
    private ImageView mojoImageView, questionImageView, mojoHatImageView, mojoShoesImageView, leftInstructionsImageView, rightInstructionsImageView, centerInstructionsImageView;//, mojoGlassesImageView;
    private View streakPopupView;
    private Persistence persistence;
    private Timer scheduler = new Timer(true);

    private Schedule schedule;
    private Queue<Medication> medicationQueue;

    private MojoClothingList clothingList;
    private ArrayList<ImageView> clothingImageViewList;
    private ArrayList<ObjectAnimator> jumpObjectAnimators;
    private ArrayList<ObjectAnimator> fallObjectAnimators;
    private AnimatorSet jumpClothingAnimations;
    private AnimatorSet fallClothingAnimations;

    private int questionYOffset;

    /**
     * The first thing to be called on app startup.
     * Most of the initialization happens here.
     *
     * @param savedInstanceState    Android caching
     * @author                      Grigory Glukhov, Aleksandra Soltan, Sasa Lekic, Julia Danek, Agnes Petajavaara, Vendela Vlk
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);
        instance = this;

        persistence = new Persistence(this);

        // Set up popups
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);
        questionImageView = questionPopup.getPopupView().findViewById(R.id.medication_image);
        questionTextView = questionPopup.getPopupView().findViewById(R.id.text_medication_question);

        streakPopup = new Popup(this, R.layout.streak_popup);
        streakPopupView = streakPopup.getPopupView();
        rewardStreakTextView = streakPopupView.findViewById(R.id.text_reward_streak);

        gameInstructionPopup = new Popup(this, R.layout.game_instruction_popup);
        gameInstructionPopup.setAnimationStyle(android.R.style.Animation_Dialog);
        leftInstructionsImageView = gameInstructionPopup.getPopupView().findViewById(R.id.left_image);
        rightInstructionsImageView = gameInstructionPopup.getPopupView().findViewById(R.id.right_image);
        centerInstructionsImageView = gameInstructionPopup.getPopupView().findViewById(R.id.center_image);
        gameInstructionTextView = gameInstructionPopup.getPopupView().findViewById(R.id.text_instruction);

        // Load sounds
        Sounds.getInstance().loadSounds(this);

        // Set up streaks
        streakView = findViewById(R.id.streak_text);
        try {
            streak = (Streak) persistence.loadObject(STREAK_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
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

        mojoImageView = (ImageView) findViewById(R.id.mojoImageView);
        mojoHatImageView = (ImageView) findViewById(R.id.mojoHatImageView);
        mojoShoesImageView = (ImageView) findViewById(R.id.mojoShoesImageView);

        mojoImageView.setImageResource(R.drawable.smiling1);

        /*
        mojoImageView.bringToFront();
        mojoHatImageView.bringToFront();
        mojoShoesImageView.bringToFront();
        */

        mojoHatImageView.setVisibility(View.INVISIBLE);
        mojoShoesImageView.setVisibility(View.INVISIBLE);

        clothingImageViewList = new ArrayList<ImageView>();
        clothingImageViewList.add(mojoHatImageView);
        clothingImageViewList.add(mojoShoesImageView);

        clothingList = new MojoClothingList();

        if(streak.getValue() == 3){
            clothingImageViewList.get(0).setVisibility(View.VISIBLE);
            clothingList.addClothing(clothingImageViewList.get(0));
        }
        else if((streak.getValue() > 3)){
            for(int i = 0; (i <= (streak.getValue() / 6)) && (i < clothingImageViewList.size()); i++){
                ImageView activeClothing = clothingImageViewList.get(i);
                activeClothing.setVisibility(View.VISIBLE);
                clothingList.addClothing(activeClothing);
            }
        }

        jumpObjectAnimators = clothingList.getJumpClothingAnimations();
        fallObjectAnimators = clothingList.getFallClothingAnimations();
        jumpClothingAnimations = new AnimatorSet();
        fallClothingAnimations = new AnimatorSet();

        questionYOffset = (int) (getResources().getDisplayMetrics().heightPixels * QUESTION_POPUP_OFFSET_Y_RELATIVE);
        congratulationTextView = findViewById(R.id.txt_greeting_congratulation);

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

    public static MojoScreen getInstance() {
        return instance;
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    congratulationTextView.setText(R.string.good_job);
                    congratulationTextView.setVisibility(View.VISIBLE);
                }
            });
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
        } catch (Exception e) {
            System.out.println("Schedule not found. Creating new one.");
            e.printStackTrace();
            schedule = Schedule.generate(time);
            try {
                persistence.saveObject(schedule, SCHEDULE_FILENAME);
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }

        schedule.connectStreak(streak);
        schedule.validateQueue(true);
    }
    /**
     * Called when options button is pressed
     *
     * Switches to options menu
     *
     * @param view Android button view that was pressed.
     */
    public void onOptionsClick(View view) {
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }

    /**
     * Called when 'play' button is pressed.
     *
     * Switches to the game activity.
     *
     * @param view Android button view that was pressed.
     */
    public void onButtonPlay(View view) {
        gameInstructionTextView.setText("Se upp för pollen och rök!");
        leftInstructionsImageView.setImageResource(R.drawable.avoidpollen);
        rightInstructionsImageView.setImageResource(R.drawable.avoidsmoke);
        leftInstructionsImageView.setVisibility(View.VISIBLE);
        rightInstructionsImageView.setVisibility(View.VISIBLE);
        centerInstructionsImageView.setImageResource(R.drawable.lungs);
        centerInstructionsImageView.setVisibility(View.INVISIBLE);
        showGameInstructionPopup();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameInstructionTextView.setText("Tryck för att flytta Mojo!");

                leftInstructionsImageView.setImageResource(R.drawable.mojoleftbubble);
                rightInstructionsImageView.setImageResource(R.drawable.tryck1);
            }
        }, MS_OBSTACLE_POPUP_DELAY);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Sounds.getInstance().playSound(Sounds.Sound.S_CLICK, 0);
                rightInstructionsImageView.setImageResource(R.drawable.tryck2);

            }
        }, MS_OBSTACLE_POPUP_DELAY + MS_MOVE_INSTRUCTION_DELAY);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                rightInstructionsImageView.setImageResource(R.drawable.tryck1);

            }
        }, MS_OBSTACLE_POPUP_DELAY + MS_MOVE_INSTRUCTION_DELAY + MS_BUTTON_DELAY);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                leftInstructionsImageView.setVisibility(View.INVISIBLE);
                rightInstructionsImageView.setImageResource(R.drawable.mojorightbubble);

            }
        }, MS_OBSTACLE_POPUP_DELAY + MS_MOVE_INSTRUCTION_DELAY + (MS_BUTTON_DELAY * 2));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                leftInstructionsImageView.setVisibility(View.INVISIBLE);
                rightInstructionsImageView.setVisibility(View.INVISIBLE);
                centerInstructionsImageView.setVisibility(View.VISIBLE);
                gameInstructionTextView.setText("Nu kör vi!");

            }
        }, MS_OBSTACLE_POPUP_DELAY + MS_MOVE_INSTRUCTION_DELAY + (MS_BUTTON_DELAY * 2) + MS_MOVE_INSTRUCTION_DELAY);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameInstructionPopup.dismissPopupWindow();
                Intent intent = new Intent(MojoScreen.this, GameScreen.class);
                intent.putExtra(GameScreen.EXTRA_STREAK_SIZE, streak.getValue());
                startActivity(intent);
            }
        }, MS_OBSTACLE_POPUP_DELAY + MS_MOVE_INSTRUCTION_DELAY + (MS_BUTTON_DELAY * 2) + MS_MOVE_INSTRUCTION_DELAY + MS_LUNG_DELAY);

    }

    /**
     * Reset the current streak.
     * Will persist the new streak!
     */
    public void resetStreak() {
        streak.reset();
    }

    /**
     * Reset the current queue.
     */
    public void resetQueue() {
        schedule.resetQueue();
        try {
            persistence.saveObject(schedule, SCHEDULE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkMedication();
    }

    /**
     * Generate a brand new schedule.
     */
    public void generateSchedule() {
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
     * Show a popup and animated Mojo reaction.
     *
     * @author Grigory Glukhov, Aleksandra Soltan, Sasa Lekic
     */
    public void showQuestionPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);
        // Set the dynamic image and name
        setPopupMedication(medicationQueue.element());
        congratulationTextView.setVisibility(View.INVISIBLE);
        // Show popup
        questionPopup.showPopupWindow(currentScreen, Gravity.CENTER, 0, questionYOffset);
    }

    public void showGameInstructionPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);
        // Set the dynamic image and name
        //setPopupMedication(medicationQueue.element());
        // Show popup
        gameInstructionPopup.showPopupWindow(currentScreen);
    }


    /**
     * Show reward streak popup, Mojo grinning animation, and update clothing if applicable
     *
     * @author Sasa Lekic, Aleksandra Soltan
     */
    public void showStreakPopup() {
        View currentScreen = findViewById(R.id.activity_mojo_screen);
        rewardStreakTextView.setText(getResources().getString(R.string.streak_popup, streak.getValue()));
        waveAnimation.stop();

        if(jumpObjectAnimators.size() < clothingImageViewList.size()){
            ImageView activeClothing = clothingImageViewList.get(jumpObjectAnimators.size());
            clothingList.addClothing(activeClothing);
            activeClothing.setVisibility(View.VISIBLE);
            jumpObjectAnimators = clothingList.getJumpClothingAnimations();
            fallObjectAnimators = clothingList.getFallClothingAnimations();
        }

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
        mojoImageView.setVisibility(View.VISIBLE);
        mojoImageView.setImageResource(R.drawable.grinning1);

        //Mojo jumps
        jumpClothingAnimations.playTogether(jumpObjectAnimators.toArray(new ObjectAnimator[jumpObjectAnimators.size()]));
        jumpClothingAnimations.start();

        ViewPropertyAnimator viewPropertyAnimator = mojoImageView.animate()
                .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Mojo falls and bounces
                        fallClothingAnimations.playTogether(fallObjectAnimators.toArray(new ObjectAnimator[fallObjectAnimators.size()]));
                        fallClothingAnimations.start();

                        mojoImageView.animate()
                                .translationY(0)
                                .setInterpolator(bounceInterpolator).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                mojoImageView.setBackgroundResource(R.drawable.arm_animation_grinning);
                                mojoImageView.setImageResource(android.R.color.transparent);
                                // Get the background, which has been compiled to an AnimationDrawable object.
                                waveAnimation = (AnimationDrawable) mojoImageView.getBackground();

                                if (grinWaveAnimationPlayed) {
                                    waveAnimation.stop();
                                }

                                // Start the animation, Mojo waves
                                waveAnimation.start();

                                Sounds.getInstance().playSound(Sounds.Sound.S_HAPPY);

                                grinWaveAnimationPlayed = true;
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

        //Mojo jumps
        if(jumpObjectAnimators.size() != 0) {
            jumpClothingAnimations.playTogether(jumpObjectAnimators.toArray(new ObjectAnimator[jumpObjectAnimators.size()]));
            jumpClothingAnimations.start();
        }

        mojoImageView.setImageResource(R.drawable.smiling1);
        ViewPropertyAnimator viewPropertyAnimator = mojoImageView.animate()
                .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Mojo falls and bounces
                        if(fallObjectAnimators.size() != 0) {
                            fallClothingAnimations.playTogether(fallObjectAnimators.toArray(new ObjectAnimator[fallObjectAnimators.size()]));
                            fallClothingAnimations.start();
                        }

                        mojoImageView.animate()
                                .translationY(0)
                                .setInterpolator(bounceInterpolator).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {

                                mojoImageView.setBackgroundResource(R.drawable.arm_animation);
                                mojoImageView.setImageResource(android.R.color.transparent);
                                // Get the background, which has been compiled to an AnimationDrawable object.
                                waveAnimation = (AnimationDrawable) mojoImageView.getBackground();

                                if (smileWaveAnimationPlayed) {
                                    waveAnimation.stop();
                                }

                                // Start the animation, Mojo waves
                                waveAnimation.start();

                                Sounds.getInstance().playSound(Sounds.Sound.S_HAPPY);

                                smileWaveAnimationPlayed = true;
                            }
                        });
                    }
        });

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

        //Play Mojo frowning animation
        mojoImageView.setBackgroundResource(R.drawable.frown_animation);
        mojoImageView.setImageResource(android.R.color.transparent);
        AnimationDrawable frownAnimation = (AnimationDrawable) mojoImageView.getBackground();

        if (frownAnimationPlayed) {
            frownAnimation.stop();
        }

        frownAnimation.start();

        // Play sad sound
        Sounds.getInstance().playSound(Sounds.Sound.S_SAD);

        frownAnimationPlayed = true;

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
     * @author Aleksandra Soltan
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
                            else if(streak.getValue() == 0){
                                //If streak is reset, remove Mojo's clothes
                                for(int i = 0; i < clothingImageViewList.size(); i++){
                                    clothingImageViewList.get(i).setVisibility(View.INVISIBLE);
                                }
                                clothingList.removeClothing();
                                jumpObjectAnimators = clothingList.getJumpClothingAnimations();
                                fallObjectAnimators = clothingList.getFallClothingAnimations();
                            }
                        }
                    });
                }
            }, MS_REWARD_STREAK_SHOW_DELAY);
        }
    }

    public void onCreateOptionMenu(MenuItem item){
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }
}