package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MojoScreen extends AppCompatActivity {

    protected Popup questionPopup;
    int streak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);

        // Set up popup.
        questionPopup = new Popup(this, R.layout.question_popup);
        questionPopup.setAnimationStyle(android.R.style.Animation_Dialog);
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

        TextView tv = findViewById(R.id.streak_text);
        streak++;
        tv.setText(Integer.toString(streak));
        // TODO: process taking medication
        questionPopup.dismissPopupWindow();
    }

    public void onButtonNo(View view) {
        questionPopup.dismissPopupWindow();
    }
}
