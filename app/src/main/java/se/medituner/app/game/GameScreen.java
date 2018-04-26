package se.medituner.app.game;
import se.medituner.app.*;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

//GameScreen view the users current score and hiScore
public class GameScreen extends AppCompatActivity {
/**
 * Game screen activity.
 * @author Agnes Petäjävaara
 */
    private Persistence persistence;
    public static final String SAVED_SCORE = "savedScore";
    TextView currentScore;
    TextView highScore;
    private int score;
    private int hiScore;
    Timer timer;
    GameSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Do the while loop as long as the game is on, when game over check if hiScore should update
        //while (game != over){
        super.onCreate(savedInstanceState);
        glSurfaceView = new GameSurfaceView(this);
        setContentView(R.layout.highscore_view);
        persistence = new Persistence(this);

        try{
            hiScore = (int) persistence.loadObject(SAVED_SCORE);
        } catch (IOException e){
            System.err.println("could not load the high score, resetting it.");
            hiScore = 0;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
        /*}
        //while loop ends - go to getHiScore:
            getHiScore();
        */
    }
    // @author Agnes
    private void timerMethod() {
        this.runOnUiThread(timer_tick);
    }//@aurhor Agnes
    private Runnable timer_tick = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            currentScore.setText("score: " + score);
            score++;
            //Check if the current score should update the hiScore:
            try {
                getHiScore();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /*
    check if the current score is higher than hiScore, if so update it!
    @author Agnes
     */
    @SuppressLint("SetTextI18n")
    public void getHiScore() throws IOException{
        System.out.println("score: " + score);
        if(score > hiScore){
            hiScore = score;
            highScore.setText("hiScore: " + score);
            persistence.saveObject(hiScore, SAVED_SCORE);
        }else{
           /* try {
                hiScore = (int) persistence.loadObject(SAVED_SCORE);
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            } */
            highScore.setText("hiScore: " + hiScore);
            persistence.saveObject(hiScore, SAVED_SCORE);
        }

    }
}
