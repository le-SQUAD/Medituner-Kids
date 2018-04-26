package se.medituner.app.game;

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

import se.medituner.app.Background;
import se.medituner.app.R;

/**
 * The scene which encompasses background and drawable objects.
 * Houses some game logic.
 *
 * @author Grigory Glukhov
 */
public class Scene implements IScene, GLSurfaceView.Renderer {

    private Context context;

    // References to drawn objects.
    private Background background;
    private Quad model;

    private static final long MS_ANIMATION_TIME = 2000l;

    // Mojo related variables.
    private static final float MOJO_SCALE = 0.6f;
    private static final float MOJO_FLOAT_MAX_DISTANCE = 0.1f;
    private static final float MOJO_Y_OFFSET = -0.75f;
    private float flipFactor = 0.6f;
    private float angle = -0.896055385f * 180.0f;

    private static final float COLORS_BACKGROUND[][] = {
        { 0.5f, 0.0431372549f, 0.0431372549f },
        { 0.73725490196f, 0.34117647058f, 0.34117647058f },
        { 0.65098039215f, 0.19215686274f, 0.19215686274f },
        { 0.85098039215f, 0.5f, 0.5f }
    };
    private static final float COLOR_DEFAULT[] = { 1.0f, 1.0f, 1.0f, 1.0f };


    private static final float TAU = (float) Math.PI * 2.0f;

    private static final short OBSTACLE_COUNT = 4;

    private Obstacle obstacles[];

    // Handles to shaders.
    private int hQuadProgram;
    private int hBackgroundProgram;

    // Handlers to textuers
    private int hTextureMojo;
    private int hTexturesObstacle[];

    // Screen (surface view) width-to-height ratio
    private float ratio;

    // Pre-allocated arrays for matricies.
    private float scaleMatrix[] = new float[16], translateMatrix[] = new float[16];
    private float rotationMatrix[] = new float[16], transformMatrix[] = new float[16];

    // RNG used by the scene.
    private Random rng;

    /**
     * Create a new scene with given app context.
     *
     * The context will be used to get textures for Mojo and obstacles.
     *
     * @param context App context to be used for loading textures.
     */
    public Scene(Context context) {
        this.context = context;
    }

    /**
     * Called once when the surface is created, most of the one-time initialization happens here.
     *
     * @param gl        unused
     * @param config    unused
     */
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

        obstacles = new Obstacle[OBSTACLE_COUNT];
        for (int i = 0; i < obstacles.length; i++)
            obstacles[i] = new Obstacle(model);
    }

    /**
     * Called after the creation of the surface and every time its dimensions change.
     *
     * @param gl        unused
     * @param width     The new width of the surface.
     * @param height    The new height of the surface.
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        background.resize(width, height);
        ratio = width / (float) height;
        Obstacle.setScreenRatio(ratio);
    }

    /**
     * Called when the frame should be drawn.
     * This is the core function for drawing a frame.
     *
     * @param gl    unused
     * @author Grigory Glukhov, Aleksandra Soltan
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        long now = SystemClock.uptimeMillis();
        float time = now % MS_ANIMATION_TIME / (float) MS_ANIMATION_TIME;

        // Draw the background
        GLES20.glUseProgram(hBackgroundProgram);
        background.draw(COLORS_BACKGROUND, time);

        // Draw obstacles
        GLES20.glUseProgram(hQuadProgram);
        for (Obstacle obstacle : obstacles) {
            if (now - obstacle.creationTime > MS_ANIMATION_TIME) {
                obstacle.set((float) (rng.nextFloat() * Math.PI * 2.0), hTextureMojo, now + rng.nextInt((int) MS_ANIMATION_TIME));
            } else if (now > obstacle.creationTime) {
                float offset = getOffset((now - obstacle.creationTime) / (float) MS_ANIMATION_TIME);
                obstacle.draw(offset);
            }
        }


        // Mojo
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, MOJO_SCALE, ratio * MOJO_SCALE, 1.0f);

        Matrix.setIdentityM(translateMatrix, 0);
        float mojoX = (float) Math.sin(time * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        float mojoY = (float) Math.cos(time * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        Matrix.translateM(translateMatrix, 0, flipFactor + mojoX, MOJO_Y_OFFSET + mojoY, 0.0f);

        Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(transformMatrix, 0, scaleMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, transformMatrix, 0);
        model.draw(COLOR_DEFAULT, transformMatrix, hTextureMojo);
    }

    /**
     * Flip Mojo to the right side of the screen.
     *
     * @author Aleksandra Soltan
     */
    public void flipMojoRight() {
        if (flipFactor < 0.0f) {
            flipFactor = -flipFactor;
            angle = -angle;
        }
    }

    /**
     * Flip Mojo to the left side of the screen.
     *
     * @author Aleksandra Soltan
     */
    public void flipMojoLeft() {
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
        return (time * time * time);
    }

    /**
     * Load an android resource as a texture.
     *
     * @param context       The app context to be used for loading the resource.
     * @param resourceId    The resource id of the texture to be loaded.
     * @return The handle to the loaded texture.
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
