package se.medituner.app;

import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class GameScreen extends AppCompatActivity {
    TextView text;
    int score;
    Timer timer;
    GameSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GameSurfaceView(this);

        setContentView(R.layout.highscore_view);

        //Set the placeholder for high score in front
        glSurfaceView = findViewById(R.id.glSurfaceViewID);
        text = findViewById(R.id.textView2);
        text.bringToFront();

        /*
        Adds to the score by one each 0.5 second
        Updates directly in game view by layout
        @Author Agnes Petäjävaara
         */
        timer = new Timer();
        score = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }
            }, 500, 500);
        }

        private void timerMethod(){
        this.runOnUiThread(timer_tick);
        }
        private Runnable timer_tick = new Runnable() {
            @Override
            public void run() {
                text.setText("score: " + score);
                score++;
                }
        };

    /*
    Get the score to be able to save it to the high score list
     */
    public int getScore() {
        return score;
    }
}
