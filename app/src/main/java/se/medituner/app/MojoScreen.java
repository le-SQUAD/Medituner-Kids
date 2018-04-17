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
import java.util.Timer;
import java.util.TimerTask;


import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MojoScreen extends AppCompatActivity {

    public static final int MS_SNOOZE_DELAY = 5000;

    private Popup questionPopup;
    private int streak = 0;
    private TextView streakView, questionView;
    private String streakPrefix, questionPrefix, questionPostfix;
    private boolean animationPlayed = false, showingAerobecautohaler;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;
    private ImageView smilingBounceMojo, smilingWaveMojo, frowningMojo, popupImage;
    private MedPopupTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup.
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);

        popupImage = questionPopup.getPopupView().findViewById(R.id.medication_image);
        questionView = questionPopup.getPopupView().findViewById(R.id.text_medication_question);

        questionPrefix = getResources().getString(R.string.popup_question_prefix);
        questionPostfix = getResources().getString(R.string.popup_question_postfix);

        // Load sounds
        Sounds.getInstance().loadSounds(this);

        // Set up streaks
        streakPrefix = getResources().getString(R.string.streak_prefix) + " ";
        streakView = findViewById(R.id.streak_text);
        streakView.setText(streakPrefix + Integer.toString(streak));

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

        final IClock now = new SystemClock();
        Calendar cal = now.now();
        Timer timer = new Timer();
        //Handler handler = new Handler();
        cal.add(Calendar.SECOND, 2);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onTimePopUp(now);
            }
        }, cal.getTime());

    }

    /**
     * Show a popup and animated Mojo reaction.
     *
     * @param view
     * @author Grigory Glukhov, Aleksandra Soltan
     */
    // Show popup by pressing the button
    public void onButtonShowPopupClick(View view) {
        showPopup();
    }

    // Show popup at set time
    public void onTimePopUp(IClock time){
        if(SchedulePopup.isItPopupTime(time)) {
            showPopup();
        }
    }
    public void showPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);

        // Dynamic image in popup
        setPopupMedication(showingAerobecautohaler
            ? Medication.AEROBEC
            : Medication.AEROBECAUTOHALER);
        showingAerobecautohaler = !showingAerobecautohaler;

        questionPopup.showPopupWindow(currentScreen);
    }

    public void setPopupMedication(Medication medication) {
        popupImage.setImageResource(Medication.getImageId(getResources(), getPackageName(), medication));
        questionView.setText(questionPrefix
                + Medication.getName(getResources(), getPackageName(), medication)
                + questionPostfix);

    }

    public void onButtonYes(View view) {
        // Increase the streak
        streakView.setText(streakPrefix + Integer.toString(++streak));

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
        streakView.setText(streakPrefix + Integer.toString(streak));

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
