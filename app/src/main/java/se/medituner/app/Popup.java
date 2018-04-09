package se.medituner.app;

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

        // Show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * Dismiss the PopupWindow.
     */
    public void dismissPopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }
}
