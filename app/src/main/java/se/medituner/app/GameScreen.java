package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GameScreen extends AppCompatActivity {

    GameSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GameSurfaceView(this);

       // setContentView(glSurfaceView);
        setContentView(R.layout.highscore_view);

        glSurfaceView = findViewById(R.id.glSurfaceViewID);
        TextView text = (TextView) findViewById(R.id.textView2);
        text.bringToFront();

    }
}
