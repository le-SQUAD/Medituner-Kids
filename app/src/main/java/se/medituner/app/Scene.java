package se.medituner.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The scene which encompasses background and drawable objects.
 */
public class Scene implements IScene, GLSurfaceView.Renderer {

    private Context context;

    // References to drawn objects.
    private Background background;
    private Quad model;

    // Handles to shaders.
    private int hQuadProgram;
    private int hBackgroundProgram;

    private float flipFactor = 0.6f;
    private static final float MOJO_Y_OFFSET = -0.75f;
    private float angle = -0.896055385f * 180.0f;

    private int hTextureMojo;

    private static final long MS_ANIMATION_TIME = 2000l;

    private static final float COLORS_BACKGROUND[][] = {
        { 0.5f, 0.0431372549f, 0.0431372549f },
        { 0.73725490196f, 0.34117647058f, 0.34117647058f },
        { 0.65098039215f, 0.19215686274f, 0.19215686274f },
        { 0.85098039215f, 0.5f, 0.5f }
    };
    private static final float COLOR_DEFAULT[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    private static final float MOJO_SCALE = 0.6f;

    /*
    { 0.37647058823f, 0.0431372549f, 0.0156862745f, 1.0f }
    { 0.4431372549f, 0.04705882352f, 0.0156862745f, 1.0f };
    { 0.98823529411f, 0.29803921568f, 0.30588235294f, 1.0f };
    { 0.6f, 0.05882352941f, 0.00784313725f, 1.0f };
    */
    private static final short OBSTACLE_COUNT = 0;
    private float color_model[][];
    private double lastAngle = 0.0;
    private double rotationRate = -0.2;
    private long creationTimes[];
    private float lastTime = 2.0f;
    private float ratio;
    private float cachedSin[], cachedCos[];
    private float scaleMatrix[] = new float[16], translateMatrix[] = new float[16];
    private float rotationMatrix[] = new float[16], transformMatrix[] = new float[16];
    private Random rng;

    public Scene(Context context) {
        this.context = context;
    }

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

        // Load textures
        hTextureMojo = loadTexture(context, R.drawable.mojoinbubble);

        rng = new Random();
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

        {
            float time = SystemClock.uptimeMillis() % MS_ANIMATION_TIME / (float) MS_ANIMATION_TIME;
            GLES20.glUseProgram(hBackgroundProgram);
            background.draw(COLORS_BACKGROUND, time);
        }

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
                //model.draw(color_model[i], transformMatrix);
            }
        }

        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, MOJO_SCALE, ratio * MOJO_SCALE, 1.0f);

        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, flipFactor, MOJO_Y_OFFSET, 0.0f);

        Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(transformMatrix, 0, scaleMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, transformMatrix, 0);
        model.draw(COLOR_DEFAULT, transformMatrix, hTextureMojo);
    }

    public void flipRight() {
        if (flipFactor < 0.0f) {
            flipFactor = -flipFactor;
            angle = -angle;
        }
    }

    public void flipLeft() {
        if (flipFactor > 0.0f) {
            flipFactor = -flipFactor;
            angle = -angle;
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

    /**
     * Load an android resource as a texture.
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(final Context context, final int resourceId) {

        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture!");
        }

        return textureHandle[0];
    }
}
