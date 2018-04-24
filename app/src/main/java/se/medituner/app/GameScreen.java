package se.medituner.app;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameScreen extends AppCompatActivity {

    GLSurfaceView glSurfaceView;
    Scene scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        scene = new Scene();

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(scene);
        //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setContentView(glSurfaceView);
    }
}
