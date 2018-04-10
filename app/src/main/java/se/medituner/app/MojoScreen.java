package se.medituner.app;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

public class MojoScreen extends AppCompatActivity {

    protected Popup questionPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup
        questionPopup = new Popup(this, R.layout.popup);
        questionPopup.setAnimationEnabled(true);
        questionPopup.setAnimationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
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
    }

    public void onButtonNo(View view) {
        questionPopup.dismissPopupWindow();
    }
}
