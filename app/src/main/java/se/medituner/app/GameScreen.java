package se.medituner.app;

import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public abstract class GameScreen extends AppCompatActivity {
    TextView currentScore;
    TextView highScore;
    int score;
    int hiScore = 0;

    Timer timer;
    GameSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //Do the while loop as long as the game is on, when game over check if hiScore should update
    //while(game != over){
        super.onCreate(savedInstanceState);

        glSurfaceView = new GameSurfaceView(this);

        setContentView(R.layout.highscore_view);

        //Set the hiScore and currentScore in front
        glSurfaceView = findViewById(R.id.glSurfaceViewID);
        highScore = findViewById(R.id.hiScoreId);
        highScore.bringToFront();
        currentScore = findViewById(R.id.currentScoreId);
        currentScore.bringToFront();

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

        //while loop ends

        getHiScore(score);
    }

    private void timerMethod() {
        this.runOnUiThread(timer_tick);
    }

    private Runnable timer_tick = new Runnable() {
        @Override
        public void run() {
            currentScore.setText("score: " + score);
            score++;
        }
    };

    //}
    //call function getHiScore when game is over:
    //getHiScore(score);

    /*
    Get the score to be able to save it to the high score list
     */
    public void getHiScore(int score) {
        if(score > hiScore){
            highScore.setText("hiScore: " + score);
            hiScore = score;
        }else{
            highScore.setText("hiScore: " + hiScore);
        }

    }


}
