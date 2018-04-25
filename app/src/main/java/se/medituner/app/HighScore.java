package se.medituner.app;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

/*
   A program that count up the high score every second in the game
   @author Agnes Petäjävaara
 */
public class HighScore extends Activity{
    TextView text;
    int score;
    Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore_view);
        text = findViewById(R.id.currentScoreId);
        text.bringToFront();

        timer = new Timer();
        score = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                text.setText("TEST: " + score);
                score++;
            }
        },1000, 1000);
    }

    public int getScore(){
        return score;

    }
}
