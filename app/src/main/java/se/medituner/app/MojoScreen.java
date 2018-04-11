package se.medituner.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class MojoScreen extends AppCompatActivity {

    public static int streak=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);
        TextView tv = findViewById(R.id.streak_text); //this is a textview for the current streak
        tv.setText(Integer.toString(streak));
//        Button b= (Button) findViewById(R.id.button);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MojoScreen.this, StreakPopUp.class));
//            }
//        });
    }

    public void onButton(View view) {
        streak++;
        TextView tv = findViewById(R.id.streak_text); //this is a textview for the current streak
        tv.setText(Integer.toString(streak));
        if (streak >= 3) {
            startActivity(new Intent(MojoScreen.this, StreakPopUp.class));
            MojoScreen.this.overridePendingTransition(R.anim.fade_in, R.anim.abc_fade_out);
            tv.setText(Integer.toString(streak));

        }


    }
}
