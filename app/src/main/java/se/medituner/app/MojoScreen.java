package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.graphics.drawable.AnimationDrawable;

public class MojoScreen extends AppCompatActivity {

    protected Popup questionPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup.
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    /**
     * Show a popup and animated Mojo reaction.
     *
     * @param view
     * @author Grigory Glukhov, Aleksandra Soltan
     */
    public void onButtonShowPopupClick(View view) {
        // Get the reference to an existing layout.
        View currentScreen = findViewById(R.id.activity_mojo_screen);

        questionPopup.showPopupWindow(currentScreen);
    }

    public void onButtonYes(View view) {
        // TODO: process taking medication
        questionPopup.dismissPopupWindow();

        final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator();
        final TimeInterpolator bounceInterpolator = new BounceInterpolator();
        final ImageView smilingBounceMojo = (ImageView) findViewById(R.id.smilingBounceMojo);
        final ImageView smilingWaveMojo = (ImageView) findViewById(R.id.smilingWaveMojo);

        //Smiling, jumping Mojo visible
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

                                smilingWaveMojo.setBackgroundResource(R.drawable.arm_animation);
                                // Get the background, which has been compiled to an AnimationDrawable object.
                                AnimationDrawable frameAnimation = (AnimationDrawable) smilingWaveMojo.getBackground();
                                // Start the animation, Mojo waves
                                frameAnimation.start();
                            }
                        });
                    }
                });
    }

    public void onButtonNo(View view) {
        questionPopup.dismissPopupWindow();
    }

}
