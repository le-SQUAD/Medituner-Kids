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
    protected int animationStyle = -1;

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
     * Show the popup window, creating one if necessary at given location.
     *
     * @param parentView The parent view for the popup.
     * @param gravity The gravity setting
     * @param x x
     * @param y y
     */
    public void showPopupWindow(View parentView, int gravity, int x, int y) {
        if (popupWindow == null) {
            // Create the popup window
            int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;   // Tapping outside of the window will still be processed by it

            popupWindow = new PopupWindow(popupView, width, height, focusable);
            popupWindow.setAnimationStyle(animationStyle);
        }

        popupWindow.showAtLocation(parentView, gravity, x, y);
    }

    /**
     * Get the reference to the view of the popup.
     *
     * @return The reference to popup view.
     */
    public View getPopupView() {
        return popupView;
    }

    /**
     * Set the animation style for the window
     *
     * @param animationStyle The animation style of the window.
     */
    public void setAnimationStyle(int animationStyle) {
        this.animationStyle = animationStyle;
    }

    /**
     * Show the popup window, creating one if necessary in the middle of the screen.
     *
     * @param parentView The parent view.
     */
    public void showPopupWindow(View parentView) {
        showPopupWindow(parentView, Gravity.CENTER, 0, 0);
    }

    /**
     * Dismiss the PopupWindow.
     */
    public void dismissPopupWindow() {
        popupWindow.dismiss();
        popupWindow = null;
    }
}
