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
    private static final float COLOR_STRIP[] = { 0.1f, 0.0f, 0.0f, 1.0f };
    private static final short OBSTACLE_COUNT = 20;
    private float color_inner[] = new float[4];
    private float color_outer[] = new float[4];
    private float color_model[][];
    private double lastAngle = 0.0;
    private double rotationRate = -0.2;
    private long creationTimes[];
    private float ratio;
    private float cachedSin[], cachedCos[];
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
        color_inner = new float[] {0.2f, 0.0f, 0.1f, 1.0f};
        color_outer = new float[] {0.3f, 0.0f, 0.0f, 1.0f};
        color_model = new float[OBSTACLE_COUNT][4];
        cachedCos = new float[OBSTACLE_COUNT];
        cachedSin = new float[OBSTACLE_COUNT];
        creationTimes = new long[OBSTACLE_COUNT];
        for (int i = 0; i < creationTimes.length; i++) {
            creationTimes[i] = SystemClock.uptimeMillis() + (i * (MS_ANIMATION_TIME / OBSTACLE_COUNT));
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        background.resize(width, height);
        ratio = width / (float) height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        for (int i = 0; i < OBSTACLE_COUNT; i++) {
            if (SystemClock.uptimeMillis() - creationTimes[i] > MS_ANIMATION_TIME) {
                creationTimes[i] = SystemClock.uptimeMillis();

                lastAngle += rotationRate;
                cachedSin[i] = (float) Math.sin(lastAngle);
                cachedCos[i] = (float) Math.cos(lastAngle);
                randomColor(color_model[i]);
            }
        }

        GLES20.glUseProgram(hBackgroundProgram);
        background.draw(color_inner, COLOR_STRIP, color_outer,
                SystemClock.uptimeMillis() % MS_ANIMATION_TIME / (float) MS_ANIMATION_TIME);

        GLES20.glUseProgram(hQuadProgram);
        for (int i = 0; i < OBSTACLE_COUNT; i++) {
            float time = (SystemClock.uptimeMillis() - creationTimes[i]) / (float) MS_ANIMATION_TIME;
            if (time > 0.0f) {
                float offset = getOffset(time);
                Matrix.setIdentityM(scaleMatrix, 0);
                Matrix.scaleM(scaleMatrix, 0, offset, offset * ratio, 1.0f);
                Matrix.setIdentityM(translateMatrix, 0);
                Matrix.translateM(translateMatrix, 0,
                        offset * cachedCos[i], offset * cachedSin[i] * ratio, 0.0f);
                Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, scaleMatrix, 0);
                model.draw(color_model[i], transformMatrix);
            }
        }
    }

    /**
     * Generate a new color and put it into given array.
     *
     * @param color
     */
    private void randomColor(float[] color) {
        for (int i = 0; i < 3; i++)
            color[i] = rng.nextFloat();
        color[3] = 1.0f;
    }

    @Override
    public float getOffset(float time) {
        return (time * time * time) * 2f;
    }
}
