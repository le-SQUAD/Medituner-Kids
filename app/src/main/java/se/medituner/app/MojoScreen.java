package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MojoScreen extends AppCompatActivity {

    int streak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);
    }

    public void onButton(View view) {

        TextView tv = findViewById(R.id.streak_text);
        streak++;
        tv.setText(Integer.toString(streak));
    }


}
