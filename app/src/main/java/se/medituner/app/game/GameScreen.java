package se.medituner.app.game;
import se.medituner.app.*;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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
    private GameSurfaceView glSurfaceView;
    private HighScore highScore;

    private TextView scoreView, highScoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Do the while loop as long as the game is on, when game over check if hiScore should update
        //while (game != over){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_view);
        persistence = new Persistence(this);

        // Initializing high score, will actually attempt to load persisted one.
        highScore = new HighScore(this, persistence);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.scene.linkHighScore(highScore);
        //Set the hiScore and currentScore in front
        highScoreView = findViewById(R.id.hiScoreId);
        highScoreView.bringToFront();
        scoreView = findViewById(R.id.currentScoreId);
        scoreView.bringToFront();

        highScore.begin();

        /*
        If "BackButton" (backarrow) pressed - goes back to MojoScreen
        @author Julia Danek
        */

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        /*
        If "BackButton" pressed - ends activity
        @author Julia Danek
        */

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
           int id = item.getItemId();

           if (id == android.R.id.home){
               //ends the activity
               this.finish();
           }
        return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onStop() {
        super.onStop();

        highScore.resetScore();
    }

    public void updateScoreTexts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                highScoreView.setText(getResources().getString(R.string.score_high, highScore.getHighScore()));
                scoreView.setText(getResources().getString(R.string.score_current, highScore.getCurrentScore()));
            }
        });
    }


}
