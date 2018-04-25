package se.medituner.app.game;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Game screen activity.
 */
public class GameScreen extends AppCompatActivity {

    GameSurfaceView gameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameSurfaceView = new GameSurfaceView(this);
        setContentView(gameSurfaceView);
    }
}
