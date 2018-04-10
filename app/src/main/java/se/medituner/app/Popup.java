package se.medituner.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.content.Context;

/**
 * A popup helper class for creation of popups created from layouts.
 *
 * @author Grigory Glukhov
 */
public class Popup {

    protected PopupWindow popupWindow = null;
    protected View popupView;
    protected boolean animationEnabled = true;

    protected int animationDuration = -1;

    /**
     * A special value for animation duration.
     * Passing it as an animation duration will make the popup use system's short animation duration.
     */
    public static final int ANIM_DURATION_SHORT = -1;

    /**
     * Create a new QuestionPopup. Will cache a view for ease of use.
     *
     * @param context Current system context for inflation of the layout.
     * @param layout_id The resource id of the layout to be used in the popup.
     */
    public Popup(Context context, int layout_id) {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(layout_id, null);
    }

    /**
     * Set if the popup fade animation should be played.
     *
     * @param enabled
     */
    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
    }

    /**
     * Set the animation duration to a given value.
     * Using special values will use system's resources to get the actual value.
     *
     * @param animationDuration
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * Show the popup window. Will create one if necessary.
     *
     * @param view The parent view for the popup.
     */
    public void showPopupWindow(View view) {
        if (popupWindow == null) {
            // Create the popup window
            int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;   // Tapping outside of the window will still be processed by it

            popupWindow = new PopupWindow(popupView, width, height, focusable);
        }

        if (animationEnabled) {
            // Set the popup initially invisible.
            popupView.setAlpha(0.0f);
            // Show the popup window
            popupView.animate()     // create a ViewAnimator
                    .alpha(1.0f)    // set the animator to animate the alpha to 1
                    .setDuration(animationDuration) // set the animation's duration
                    .setListener(null); // clear any listeners
        }
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * Dismiss the PopupWindow.
     */
    public void dismissPopupWindow() {
        if (popupWindow != null) {
            if (animationEnabled) {
                popupView.animate()
                        .alpha(0.0f)
                        .setDuration(animationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                dismiss();
                            }
                        });
            } else
                dismiss();
        }
    }

    protected void dismiss() {
        popupWindow.dismiss();
        popupWindow = null;
    }
}
