package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;


import static android.view.Gravity.CENTER;

public class MojoScreen extends AppCompatActivity {

    public static final int MS_SNOOZE_DELAY = 5000;
    public static final int MS_REWARD_STREAK_ANIMATION_DURATION = 2000;
    public static final int MS_REWARD_STREAK_HIDE_DELAY = 7000;

    private Popup questionPopup, streakPopup;
    private int streak = 0;
    private TextView streakView, questionView;
    private boolean animationPlayed = false, showingAerobecautohaler;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;
    private ImageView smilingBounceMojo, smilingWaveMojo, frowningMojo, popupImage;
    private View streakPopupView;

    final Handler handler = new Handler();
    private MedPopupTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup.
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);

        streakPopup = new Popup(this, R.layout.popupstreak_object_orient);
        //streakPopup.setAnimationStyle(android.R.style.Animation_Toast);
        streakPopupView = streakPopup.getPopupView();

        popupImage = questionPopup.getPopupView().findViewById(R.id.medication_image);
        questionView = questionPopup.getPopupView().findViewById(R.id.text_medication_question);

        // Load sounds
        Sounds.getInstance().loadSounds(this);

        // Set up streaks
        streakView = findViewById(R.id.streak_text);
        streakView.setText(getResources().getString(R.string.streak, streak));

        accelerateInterpolator = new AccelerateInterpolator();
        bounceInterpolator = new BounceInterpolator();
        smilingBounceMojo = (ImageView) findViewById(R.id.smilingBounceMojo);
        smilingWaveMojo = (ImageView) findViewById(R.id.smilingWaveMojo);
        frowningMojo = (ImageView) findViewById(R.id.frowningMojo);
        smilingBounceMojo.bringToFront();
        smilingWaveMojo.bringToFront();
        frowningMojo.bringToFront();

        //timer = new Timer();
        timer = new MedPopupTimer();
    }

    /**
     * Show a popup and animated Mojo reaction.
     *
     * @param view
     * @author Grigory Glukhov, Aleksandra Soltan, Sasa Lekic
     */
    public void onButtonShowPopupClick(View view) {
        showPopup();
    }

    public void showPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);

        // Dynamic image in popup
        setPopupMedication(showingAerobecautohaler
            ? Medication.AEROBEC
            : Medication.AEROBECAUTOHALER);
        showingAerobecautohaler = !showingAerobecautohaler;

        // questionPopup.showPopupWindow(currentScreen);
        //streakPopup.showPopupWindow(currentScreen);
        if( ++streak > 3 ) {
            Sounds.getInstance().playSound(Sounds.Sound.S_STAR1);
            streakPopupView.setScaleX(0.0f);
            streakPopupView.setScaleY(0.0f);
            streakPopupView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(MS_REWARD_STREAK_ANIMATION_DURATION);
            streakPopup.showPopupWindow(currentScreen, CENTER, 0, -240);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Sounds.getInstance().playSound(Sounds.Sound.S_STAR2);
                    streakPopupView.animate()
                            .scaleX(0.0f)
                            .scaleY(0.0f)
                            .setDuration(MS_REWARD_STREAK_ANIMATION_DURATION)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    streakPopup.dismissPopupWindow();
                                }
                            });
                }
            }, MS_REWARD_STREAK_HIDE_DELAY);
        }


        questionPopup.showPopupWindow(currentScreen);
    }

    public void setPopupMedication(Medication medication) {
        popupImage.setImageResource(Medication.getImageId(getResources(), getPackageName(), medication));
        questionView.setText(getResources().getString(R.string.popup_question,
                Medication.getName(getResources(), getPackageName(), medication)));
    }

    public void onButtonYes(View view) {
        // Increase the streak
        streakView.setText(getResources().getString(R.string.streak, ++streak));

        // Play jump sound
        Sounds.getInstance().playSound(Sounds.Sound.S_JUMP);

        // TODO: process taking medication
        // Hide the popup
        questionPopup.dismissPopupWindow();

        //Smiling, jumping Mojo visible
        frowningMojo.setVisibility(View.INVISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);
        smilingBounceMojo.setVisibility(View.VISIBLE);

        //Mojo jumps
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

    public void onButtonNo(View view) {
        streak = 0;
        streakView.setText(getResources().getString(R.string.streak, streak));

        // Hide the popup
        questionPopup.dismissPopupWindow();

        //Frowning Mojo visible
        frowningMojo.setVisibility(View.VISIBLE);
        smilingWaveMojo.setVisibility(View.INVISIBLE);
        smilingBounceMojo.setVisibility(View.INVISIBLE);

        frowningMojo.setBackgroundResource(R.drawable.frown_animation);
        AnimationDrawable frownAnimation = (AnimationDrawable) frowningMojo.getBackground();

        if(animationPlayed){
            frownAnimation.stop();
        }

        frownAnimation.start();

        // Play sad sound
        Sounds.getInstance().playSound(Sounds.Sound.S_SAD);

        animationPlayed = true;

        // Set timer to ask if medication taken again
        timer.setPopupTimer();
    }

    private class MedPopupTimer{
        public void setPopupTimer() {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPopup();
                }
            }, MS_SNOOZE_DELAY);
        }
    }


}
