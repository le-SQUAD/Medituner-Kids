package se.medituner.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import se.medituner.app.game.HighScore;

public class OptionsScreen extends AppCompatActivity {

    public static final String PREFERENCE_NAME = BuildConfig.APPLICATION_ID + "PREFERENCES";
    public static final String KEY_GAME_SPEED = "GAME_SPEED";
    public static final float MIN_GAME_SPEED = 0.1f;
    public static final float MAX_GAME_SPEED = 1.0f;
    public static final float DEFAULT_GAME_SPEED = 1.0f;

    private SeekBar gameSpeedSeekBar;
    private SharedPreferences sharedPreferences;
    private float gameSpeed;
    private TextView gameSpeedText;

    /**
     * Options menu with buttons
     *
     * @param savedInstanceState Android caching
     * @author Vendela Vlk, Julia Danek, Grigory Glukhov
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_screen);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        sharedPreferences = getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );
        gameSpeed = sharedPreferences.getFloat(KEY_GAME_SPEED, DEFAULT_GAME_SPEED);
        gameSpeedSeekBar = findViewById(R.id.sb_game_speed);
        gameSpeedSeekBar.setProgress(gameSpeedToProgress(gameSpeed, gameSpeedSeekBar.getMax()));

        gameSpeedSeekBar.setOnSeekBarChangeListener(new GameSpeedBarListener());

        gameSpeedText = findViewById(R.id.txt_game_speed);
        gameSpeedText.setText(getResources().getString(R.string.game_speed, gameSpeed));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == android.R.id.home){
            //ends the activity
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onButtonResetHighScore(View view) {
        Persistence persistence = new Persistence(this);

        HighScore.resetHighScore(persistence);
    }

    public void onButtonGenerateSchedule(View view) {
        MojoScreen.getInstance().generateSchedule();
    }

    public void onButtonResetQueue(View view) {
        MojoScreen.getInstance().resetQueue();
    }

    public void onButtonResetStreak(View view) {
        MojoScreen.getInstance().resetStreak();
    }

    private class GameSpeedBarListener implements SeekBar.OnSeekBarChangeListener {

        private int max;
        private int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.progress = progress;
            gameSpeed = progressToGameSpeed(progress, max);
            gameSpeedText.setText(getString(R.string.game_speed, gameSpeed));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            max = seekBar.getMax();
            progress = seekBar.getProgress();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            gameSpeed = progressToGameSpeed(progress, max);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(KEY_GAME_SPEED, gameSpeed);
            editor.apply();
        }
    }

    private static int gameSpeedToProgress(float speed, int max) {
        speed = (speed - MIN_GAME_SPEED) / (MAX_GAME_SPEED - MIN_GAME_SPEED);
        return (int) (speed * max);
    }

    private static float progressToGameSpeed(int progress, int max) {
        float speed = progress / (float) max;
        speed = speed * (MAX_GAME_SPEED - MIN_GAME_SPEED) + MIN_GAME_SPEED;
        return speed;
    }
}

