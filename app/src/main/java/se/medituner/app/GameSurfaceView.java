package se.medituner.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameSurfaceView extends GLSurfaceView{
    private GameRenderer renderer;

    public GameSurfaceView(Context context) {
        super(context);
    }

    public void start() {
        renderer = new GameRenderer();;
        setRenderer(renderer);
    }

}
