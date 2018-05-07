package se.medituner.app.game;
import se.medituner.app.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

/**
 * Game screen activity.
 *
 * @author Agnes Petäjävaara, Julia Danek, Grigory Glukhov
 */
public class GameScreen extends AppCompatActivity {

    public static final String EXTRA_STREAK_SIZE = "STREAK";

    private static final double GROWTH_SLOWDOWN_POINT = 21.0;
    private static final double MAX_INVULNERABILITY_TIME = 30000.0;

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

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing high score, will actually attempt to load persisted one.
        highScore = new HighScore(this, persistence);

        long invulnerabilityTime = streakToInvulnerabilityTime(getIntent().getIntExtra(EXTRA_STREAK_SIZE, 0));

        SharedPreferences sp = getSharedPreferences(
                OptionsScreen.PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );
        float gameSpeed = sp.getFloat(OptionsScreen.KEY_GAME_SPEED, OptionsScreen.DEFAULT_GAME_SPEED);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.scene.linkHighScore(highScore);
        glSurfaceView.scene.setMojoInvulnerabilityTime(invulnerabilityTime);
        glSurfaceView.scene.setBackgroundSpeed(gameSpeed);
        //Set the hiScore and currentScore in front
        highScoreView = findViewById(R.id.hiScoreId);
        highScoreView.bringToFront();
        scoreView = findViewById(R.id.currentScoreId);
        scoreView.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.scene.resetMojoInvulnerability();

        /**
         * Implementing looping game sound
         * @author Julia Danek
         */
        Sounds.getInstance().playSound(Sounds.Sound.S_GSONG, -1);
        highScore.begin();
    }


    /**
     * Process selection of "back" button on the banner.
     *
     * @param item  The item that was selected.
     *
     * @return  True if the action is to be consumed.
     *
     * @author Julia Danek
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       switch (item.getItemId()) {
           case android.R.id.home:
               this.finish();
               return true;

           default:
               return super.onOptionsItemSelected(item);
       }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Sounds.getInstance().stopSound();
        highScore.resetScore();
        highScore.stop();
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

    /**
     * Streak to invulnerability time conversion.
     *
     * @param streak    Current streak size.
     * @return          Corresponding invulnerability time for the given streak size.
     */
    public static long streakToInvulnerabilityTime(int streak) {
        if (streak <= 0)
            return 0;
        else {
            double adjustedStreak = streak / GROWTH_SLOWDOWN_POINT;
            return (long) (adjustedStreak / (adjustedStreak + 1.0) * MAX_INVULNERABILITY_TIME);
        }
    }
}
