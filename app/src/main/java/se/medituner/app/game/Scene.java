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
    private static final float MOJO_OFFSET = 0.75f;
    private Lane mojoLane = Lane.LANE_LEFT;
    private float mojoX, mojoY;

    private static final float COLORS_BACKGROUND[][] = {
        { 0.5f, 0.0431372549f, 0.0431372549f },
        { 0.73725490196f, 0.34117647058f, 0.34117647058f },
        { 0.65098039215f, 0.19215686274f, 0.19215686274f },
        { 0.85098039215f, 0.5f, 0.5f }
    };
    private static final float COLOR_DEFAULT[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static final float TAU = (float) Math.PI * 2.0f;
    private static final short OBSTACLE_COUNT = 5;
    private static final Lane LANES[] = Lane.values();

    private static long obstacleBreak;

    private Obstacle obstacles[];

    // Handles to shaders.
    private int hQuadProgram;
    private int hBackgroundProgram;

    // Handlers to textures
    private int hTextureMojo;
    private int hTexturesObstacle[];

    // Screen (surface view) width-to-height ratio
    private float ratio, invRatio;

    // Pre-allocated arrays for matrices.
    private float scaleMatrix[] = new float[16], translateMatrix[] = new float[16];
    private float rotateMatrix[] = new float[16], transformMatrix[] = new float[16];

    private float laneAngles[] = {
            (float) -Math.PI * 1.0f / 4.0f,
            (float) -Math.PI * 3.0f / 4.0f
    };
    private float mojoAngle;

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
        hTexturesObstacle = new int[2];
        hTexturesObstacle[0] = loadTexture(context, R.drawable.pollenobstacle);
        hTexturesObstacle[1] = loadTexture(context, R.drawable.smokeobstacle);

        rng = new Random();

        obstacleBreak = MS_ANIMATION_TIME / OBSTACLE_COUNT;
        obstacles = new Obstacle[OBSTACLE_COUNT];
        for (int i = 0; i < obstacles.length; i++) {
            obstacles[i] = new Obstacle(model);
        }
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
        invRatio = (float) height / (float) width;

        // Mojo scaling will remain the same for a given ratio
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, MOJO_SCALE, ratio * MOJO_SCALE, 1.0f);

        updateMojo();
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
        for (int i = 0; i < obstacles.length; i++) {
            if (now - obstacles[i].creationTime > MS_ANIMATION_TIME) {
                Lane lane = LANES[rng.nextInt(LANES.length)];
                obstacles[i].set(getLaneAngle(lane) + getRandomAngleOffset(),
                        hTexturesObstacle[rng.nextInt(hTexturesObstacle.length)],
                        clampTime(now, i),
                        lane);
            } else if (now > obstacles[i].creationTime) {
                float offset = getOffset((now - obstacles[i].creationTime) / (float) MS_ANIMATION_TIME);
                obstacles[i].draw(offset);
            }
        }


        // Mojo
        Matrix.setIdentityM(translateMatrix, 0);
        float mojoOffsetX = (float) Math.sin(time * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        float mojoOffsetY = (float) Math.cos(time * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        Matrix.translateM(translateMatrix, 0, mojoX + mojoOffsetX, mojoY + mojoOffsetY, 0.0f);

        setMojoRotationMatrix(rotateMatrix, mojoX + mojoOffsetX, (mojoY + mojoOffsetY) * invRatio);
        Matrix.rotateM(rotateMatrix, 0,
                90.0f, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(transformMatrix, 0, scaleMatrix, 0, rotateMatrix, 0);
        Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, transformMatrix, 0);

        model.draw(COLOR_DEFAULT, transformMatrix, hTextureMojo);
    }

    /**
     * Sets up the mojo rotation matrix for him to look at the centre.
     *
     * @param matrix
     */
    private void setMojoRotationMatrix(float[] matrix, float x, float y) {
        float dist = (float) Math.sqrt(x * x + y * y);
        float cos = x / dist;
        float sin = y / dist;

        matrix[0] = cos;
        matrix[1] = sin;
        matrix[2] = 0.0f;
        matrix[3] = 0.0f;

        matrix[4] = -sin;
        matrix[5] = cos;
        matrix[6] = 0.0f;
        matrix[7] = 0.0f;

        matrix[8] = 0.0f;
        matrix[9] = 0.0f;
        matrix[10] = 1.0f;
        matrix[11] = 0.0f;

        matrix[12] = 0.0f;
        matrix[13] = 0.0f;
        matrix[14] = 0.0f;
        matrix[15] = 1.0f;
    }

    /**
     * Set the Mojo's lane to provided one.
     *
     * @param lane The new Mojo lane.
     */
    public void setMojoLane(Lane lane) {
        if (mojoLane != lane) {
            mojoLane = lane;
            updateMojo();
        }
    }


    private void updateMojo() {
        mojoAngle = getLaneAngle(mojoLane);
        mojoX = (float) Math.cos(mojoAngle) * MOJO_OFFSET * invRatio;
        mojoY = (float) Math.sin(mojoAngle) * MOJO_OFFSET * invRatio;

        mojoAngle = (mojoAngle * 180.0f / (float) Math.PI) + 90.0f;
    }

    private float getLaneAngle(Lane lane) {
        switch (lane) {
            case LANE_LEFT:
                return laneAngles[1];

            case LANE_RIGHT:
                return laneAngles[0];

            default:
                return 0.0f;
        }
    }

    private float getRandomAngleOffset() {
        return (rng.nextFloat() - 0.5f) / 3.5f;
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

    private long clampTime(long now, int i) {
        return now - now % MS_ANIMATION_TIME + obstacleBreak * i;
    }


    @Override
    public float getOffset(float time) {
        return time * time * time;
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
