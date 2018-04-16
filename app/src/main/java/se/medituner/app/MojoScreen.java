package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;

public class MojoScreen extends AppCompatActivity {
    
    private Popup questionPopup;
    private int streak = 0;
    private TextView streakView;
    private String streakPrefix;
    private boolean animationPlayed = false, showingAerobecautohaler;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;
    private ImageView smilingBounceMojo, smilingWaveMojo, frowningMojo, popupImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup.
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);

        popupImage = questionPopup.getPopupView().findViewById(R.id.medication_image);

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
    }

    /**
     * Show a popup and animated Mojo reaction.
     *
     * @param view
     * @author Grigory Glukhov, Aleksandra Soltan
     */
    public void onButtonShowPopupClick(View view) {
        showPopup();
    }

    public void onTimePopUp(IClock time){
        if(SchedulePopup.isItPopupTime(time)) {
            showPopup();
        }
    }

    public void showPopup() {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);

        // Dynamic image in popup
        popupImage.setImageResource(showingAerobecautohaler
                ? R.mipmap.airvirospiromax1
                : R.mipmap.aerobecautohaler1);
        showingAerobecautohaler = !showingAerobecautohaler;

        questionPopup.showPopupWindow(currentScreen);

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

    }

}
