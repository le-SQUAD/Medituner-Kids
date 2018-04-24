package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameScreen extends AppCompatActivity {

    GameSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GameSurfaceView(this);

        setContentView(glSurfaceView);
        //glSurfaceView.requestRender();
    }
}
