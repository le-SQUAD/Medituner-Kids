package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.PopupWindow;
import android.widget.ImageView;
import android.view.animation.TranslateAnimation;
import android.view.animation.Interpolator;

public class MojoScreen extends AppCompatActivity {

    protected Popup questionPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);
        questionPopup = new Popup(this, R.layout.popup);
    }

    /**
     * Show a popup.
     *
     * @param view
     * @author Grigory Glukhov
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
        final ImageView image = (ImageView) findViewById(R.id.imageView2);
        ViewPropertyAnimator viewPropertyAnimator = image.animate()
                .translationY(-500).setInterpolator(accelerateInterpolator).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        image.animate()
                                .translationY(0)
                                .setInterpolator(bounceInterpolator).setDuration(1000);
                    }
                });

    }

    public void onButtonNo(View view) {
        questionPopup.dismissPopupWindow();
    }

}
