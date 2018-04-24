package se.medituner.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The scene which encompasses background and drawable objects.
 */
public class Scene implements IScene, GLSurfaceView.Renderer {

    // References to drawn objects.
    private Background background;
    private Quad model;

    // Handles to shaders.
    private int hQuadProgram;
    private int hBackgroundProgram;

    private static final long MS_ANIMATION_TIME = 2000l;
    private float colors[][] = {
        { 0.8f, 0.0f, 0.0f, 1.0f },
        { 0.5f, 0.0f, 0.0f, 1.0f },
        { 0.7f, 0.7f, 0.7f, 1.0f }
    };
    private float lastTime = 2.0f;
    private float ratio;
    private float cachedSin, cachedCos;
    private float scaleMatrix[] = new float[16], translateMatrix[] = new float[16], transformMatrix[] = new float[16];
    private Random rng;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Compile shaders
        hQuadProgram = GLES20.glCreateProgram();
        hBackgroundProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(hQuadProgram, Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.QUAD_VERTEX));
        GLES20.glAttachShader(hQuadProgram, Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.QUAD_FRAGMENT));

        GLES20.glAttachShader(hBackgroundProgram, Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.BACKGROUND_VERTEX));
        GLES20.glAttachShader(hBackgroundProgram, Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.BACKGROUND_FRAGMENT));

        GLES20.glLinkProgram(hQuadProgram);
        GLES20.glLinkProgram(hBackgroundProgram);

        // Set up gl variables to enable transparency
        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Create objects that will get drawn.
        background = Background.getInstance(hBackgroundProgram);
        model = new Quad(hQuadProgram,1.0f, 1.0f);

        rng = new Random();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        background.resize(width, height);
        ratio = width / (float) height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float time = SystemClock.uptimeMillis() % MS_ANIMATION_TIME / (float) MS_ANIMATION_TIME * 2.0f;
        float offset = getOffset(time);

        if (lastTime > time) {
            double angle = rng.nextDouble() * Math.PI * 2.0f;
            cachedSin = (float) Math.sin(angle);
            cachedCos = (float) Math.cos(angle);
            colors[2] = randomColor();
        }
        lastTime = time;

        GLES20.glUseProgram(hBackgroundProgram);
        background.draw(colors[0], colors[1], time);
        GLES20.glUseProgram(hQuadProgram);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, offset, offset * ratio, 1.0f);
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0,
                offset * cachedCos, offset * cachedSin * ratio, 0.0f);
        Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, scaleMatrix, 0);
        model.draw(colors[2], transformMatrix);
    }

    private float[] randomColor() {
        float color[] = new float[4];
        for (int i = 0; i < color.length; i++)
            color[i] = rng.nextFloat();
        return color;
    }

    @Override
    public float getOffset(float time) {
        return time * time * time;
    }
}
