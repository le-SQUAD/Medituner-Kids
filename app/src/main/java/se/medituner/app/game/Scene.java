package se.medituner.app.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.SystemClock;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import se.medituner.app.Background;
import se.medituner.app.R;
import se.medituner.app.Sounds;

/**
 * The scene which encompasses background and drawable objects.
 * Houses some game logic.
 *
 * @author Grigory Glukhov
 */
public class Scene implements IScene, GLSurfaceView.Renderer {

    private Context context;
    private HighScore highScore;

    // References to drawn objects.
    private Background background;
    private Quad model;

    private static final long MS_ANIMATION_TIME = 2000L;
    private static final long MS_MOJO_HIT_COOLDOWN = 140L;
    private static final long MS_RAINBOW_TRANSITION_DURATION = 1000L;

    private static final float COLLISION_MIN_OFFSET = 0.45f;
    private static final float COLLISION_MAX_OFFSET = 0.75f;

    // To get this one needs to use inverse offset function (in our case it's O^-1(x) = x^(1/3)).
    // To obtain the following number one needs to:
    // MIN_PERIOD = Ceiling(1.0/(O^-1(MAX_OFFSET)-O^-1(MIN_OFFSET)) * ANIMATION_TIME)
    private static final long MS_MIN_OBSTACLE_PERIOD = 285;
    private static final long MS_MAX_OBSTACLE_PERIOD = 1000;

    private static final float OBSTACLE_PERIOD_OFFSET = 2.79525482923f;
    private static final float OBSTACLE_MIN_PERIOD = 0.14225086402f;

    private float backgroundSpeed = 1.0f;

    private static final short OBSTACLE_COUNT = 8;

    private long gameStartTime;
    private long lastObstacleCreation;

    private static final float LOWEST_COLOR = 0.75f;

    private static final float COLORS_BACKGROUND_DEFAULT[][] = {
            { 0.5f, 0.0431372549f, 0.0431372549f },
            { 0.73725490196f, 0.34117647058f, 0.34117647058f },
            { 0.65098039215f, 0.19215686274f, 0.19215686274f },
            { 0.85098039215f, 0.5f, 0.5f }
    };

    private static final float COLORS_BACKGROUND_RAINBOW[][] = {
            {0.70588235f, 0.09803922f, 0.21960784f}, // Red
            {0.97647059f, 0.60784314f, 0.22352941f}, // Yellow
            {0.18823529f, 0.64313725f, 0.58431373f}, // Greenblue
            {0.60392157f, 0.34901961f, 0.70980392f}  // Violet
    };

    private float backgroundColors[][] = new float[4][3];

    private static final float COLOR_DEFAULT[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static final float TAU = (float) Math.PI * 2.0f;
    private static final Lane LANES[] = Lane.values();

    // Mojo related variables.
    private static final float MOJO_SCALE = 0.6f;
    private static final float MOJO_FLOAT_MAX_DISTANCE = 0.05f;
    private static final float MOJO_OFFSET = 0.7f;
    private Lane mojoLane = Lane.LANE_LEFT;
    private float mojoX, mojoY;
    private float mojoColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    private long lastMojoHit = -MS_MOJO_HIT_COOLDOWN;
    private long mojoInvulnerabilityDuration;
    private volatile long mojoInvulnerabilityEnd = 0;

    private static long obstacleBreak;
    private long lastFrameTime;

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
    private float transformMatrix[] = new float[16];

    // Pre-calculated angles for lanes.
    private float laneAngles[] = {
            (float) -Math.PI * 1.0f / 4.0f,
            (float) -Math.PI * 3.0f / 4.0f
    };

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

        lastFrameTime = SystemClock.uptimeMillis();
        gameStartTime = SystemClock.uptimeMillis();
        lastObstacleCreation = SystemClock.uptimeMillis();
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

        /*
        // Mojo scaling will remain the same for a given ratio
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, MOJO_SCALE, ratio * MOJO_SCALE, 1.0f);
        */

        updateMojoPositionAndRotation();
        Obstacle.setScreenRatio(ratio);
    }

    /**
     * Link a HighScore to be reset when a collision happens.
     *
     * @param highScore The HighScore to reset.
     */
    public void linkHighScore(HighScore highScore) {
        this.highScore = highScore;
    }

    /**
     * Collide Mojo, updating the hit-time and resetting the score.
     *
     * @param moment    The moment of the collision.
     */
    private void collideMojo(long moment) {
        lastMojoHit = moment;
        gameStartTime = moment;
        lastObstacleCreation = moment;
        for (int i = 0; i < obstacles.length; i++)
            if (obstacles[i].creationTime > moment)
                obstacles[i].creationTime = moment - MS_ANIMATION_TIME;

        // cough sound
       // Sounds.getInstance().playSound(Sounds.Sound.S_COUGH);

        if (highScore != null)
            highScore.resetScore();
    }

    public void resetGameStartTime() {
        gameStartTime = SystemClock.uptimeMillis();
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
        long animPeriod = (long) (MS_ANIMATION_TIME / backgroundSpeed);
        float backgroundTime = now % animPeriod / (float) animPeriod;

        // Draw the background
        GLES20.glUseProgram(hBackgroundProgram);
        if (now > mojoInvulnerabilityEnd
                || (mojoInvulnerabilityEnd - now) > MS_RAINBOW_TRANSITION_DURATION) {
            float[][] colors = (mojoInvulnerabilityEnd > now)
                    ? COLORS_BACKGROUND_RAINBOW.clone()
                    : COLORS_BACKGROUND_DEFAULT.clone();
            background.draw(colors, backgroundTime);
        } else {
            float x = (mojoInvulnerabilityEnd - now) / (float) MS_RAINBOW_TRANSITION_DURATION;
            for (int i = 0; i < backgroundColors.length; i++) {
                backgroundColors[i] = lerpColor(backgroundColors[i],
                        COLORS_BACKGROUND_DEFAULT[i],
                        COLORS_BACKGROUND_RAINBOW[i],
                        x);
            }
            background.draw(backgroundColors, backgroundTime);
        }

        // Draw obstacles
        GLES20.glUseProgram(hQuadProgram);
        for (int i = 0; i < obstacles.length; i++) {
            if (now - obstacles[i].creationTime > MS_ANIMATION_TIME) {
                Lane lane = LANES[rng.nextInt(LANES.length)];
                obstacles[i].set(getLaneAngle(lane) + getRandomAngleOffset(),
                        hTexturesObstacle[rng.nextInt(hTexturesObstacle.length)],
                        findNextCreationTime(now),
                        lane);
            } else if (now > obstacles[i].creationTime) {
                float offset = getOffset((now - obstacles[i].creationTime) / (float) MS_ANIMATION_TIME);
                checkCollision(offset, obstacles[i].lane, now);
                obstacles[i].draw(offset);
            }
        }

        float mojoOffsetX = (float) Math.sin(backgroundTime * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        float mojoOffsetY = (float) Math.cos(backgroundTime * TAU) * MOJO_FLOAT_MAX_DISTANCE;
        setMojoMatrix(transformMatrix, mojoX + mojoOffsetX, mojoY + mojoOffsetY);

        mojoColor[0] = mojoColor[1] = mojoColor[2] =
                clamp01((now - lastMojoHit) / (float) MS_MOJO_HIT_COOLDOWN)
                        * (1.0f - LOWEST_COLOR) + LOWEST_COLOR;

        model.draw(mojoColor, transformMatrix, hTextureMojo);
        lastFrameTime = now;
    }

    /**
     * Sets up the complete Mojo transformation matrix including scale, translation and rotation..
     *
     * @param matrix    The matrix to set.
     * @param x         The Mojos X position.
     * @param y         The Mojos Y position.
     * @author Grigory Glukhov
     */
    private void setMojoMatrix(float[] matrix, float x, float y) {
        float dist = (float) Math.sqrt(x * x + y * y);
        float cos = x / dist;
        float sin = y / dist;

        /*
        The resulting matrix is the result of several 2D transformations, from top to bottom:

        *
        [Scaling (fake projection)]
        [MOJO_SCALE     0]
        [0              MOJO_SCALE * SCREEN_RATIO]
        *
        [Actual rotation]
        [cos    -sin]
        [sin    cos]
        *
        [90 degree rotation]
        [0      -1]
        [1      0]
        *
        [Translation]
        [0      0       x]
        [0      0       y]
        [0      0       1]

        If you understand this, great job, you know linear algebra.
         */

        // Column 1
        matrix[0] = -sin * MOJO_SCALE;
        matrix[1] = cos * MOJO_SCALE * ratio;
        matrix[2] = 0.0f;
        matrix[3] = 0.0f;

        // Column 2
        matrix[4] = -cos * MOJO_SCALE;
        matrix[5] = -sin * MOJO_SCALE * ratio;
        matrix[6] = 0.0f;
        matrix[7] = 0.0f;

        // Column 3
        matrix[8] = 0.0f;
        matrix[9] = 0.0f;
        matrix[10] = 1.0f;
        matrix[11] = 0.0f;

        // Column 4
        matrix[12] = x;
        matrix[13] = y;
        matrix[14] = 0.0f;
        matrix[15] = 1.0f;
    }

    /**
     * Set new duration for Mojos invulnerability in millisecond.
     *
     * @param duration  The duration starting 'now' for Mojos invulnerability.
     */
    public void setMojoInvulnerabilityTime(long duration) {
        mojoInvulnerabilityDuration = duration;
        resetMojoInvulnerability();
    }

    /**
     * Reset (enable) Mojos invulnerability.
     */
    public void resetMojoInvulnerability() {
        mojoInvulnerabilityEnd = SystemClock.uptimeMillis() + mojoInvulnerabilityDuration;
    }

    /**
     * Set the Mojos lane to provided one.
     *
     * @param lane The new Mojo lane.
     */
    public void setMojoLane(Lane lane) {
        if (mojoLane != lane) {
            mojoLane = lane;
            updateMojoPositionAndRotation();
        }
    }

    /**
     * Toggle Mojos lane.
     *
     * If Mojo was in right lane toggle it to left and vice-versa.
     */
    public void toggleMojoLane() {
        if (mojoLane == Lane.LANE_LEFT)
            setMojoLane(Lane.LANE_RIGHT);
        else
            setMojoLane(Lane.LANE_LEFT);
    }

    /**
     * Get remaining amount of Mojos invulnerability time.
     *
     * @return  Mojos remaining invulnerability time.
     */
    public long getRemainingMojoInvulnerability() {
        if (lastFrameTime >= mojoInvulnerabilityEnd)
            return 0;
        else
            return mojoInvulnerabilityEnd - lastFrameTime;
    }

    /**
     * Clamp a floating number between 0 and 1 (set it to 0 if it's less than 0 or to 1 if it's bigger than 1).
     *
     * @param x The number to clamp.
     * @return  The clamped number.
     */
    private float clamp01(float x) {
        if (x < 0.0f)
            return 0.0f;
        else if (x > 1.0f)
            return 1.0f;
        else
            return x;
    }

    /**
     * Update Mojos position and rotation angle.
     */
    private void updateMojoPositionAndRotation() {
        float mojoAngle = getLaneAngle(mojoLane);
        mojoX = (float) Math.cos(mojoAngle) * MOJO_OFFSET;
        mojoY = (float) Math.sin(mojoAngle) * MOJO_OFFSET * invRatio;
    }

    /**
     * Get the corresponding angle for a given lane.
     *
     * @param lane  The lane for which to retrieve the angle.
     * @return      The corresponding angle for the given lane.
     */
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

    /**
     * Pick a random angle offset in radians.
     *
     * @return A random angle for offset.
     */
    private float getRandomAngleOffset() {
        return (rng.nextFloat() - 0.5f) / 3.5f;
    }

    /**
     * Check if a certain offset collides with Mojo.
     *
     * @param offset    The obstacles offset to check.
     * @param lane      The lane in which the obstacle is located.
     * @param moment    The current moment in time.
     */
    private void checkCollision(float offset, Lane lane, long moment) {
        if (lane == mojoLane
                && moment > mojoInvulnerabilityEnd
                && offset >= COLLISION_MIN_OFFSET
                && offset <= COLLISION_MAX_OFFSET) {
                collideMojo(moment);
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

    /**
     * Clamp the time for obstacle spawning.
     *
     * @param now   Current time.
     * @param i     Obstacle's offset.
     * @return      New obstacle's spawn time.
     */
    private long findNextCreationTime(long now) {
        /*
        Lerp over 1 minute
        float time = (now - gameStartTime) / (float) 60000;
        long spawnOffset = (long) (MS_MAX_OBSTACLE_PERIOD * (1.0f - time))
                + (long) (MS_MIN_OBSTACLE_PERIOD * time);
        lastObstacleCreation += spawnOffset;
        return lastObstacleCreation;
        */
        float time = (now - gameStartTime) / (float) 1000;
        float period = 1.0f / (time + OBSTACLE_PERIOD_OFFSET) + OBSTACLE_MIN_PERIOD;
        long timeOffset = (long) (period * MS_ANIMATION_TIME);
        lastObstacleCreation += timeOffset;
        return lastObstacleCreation;
    }

    @Override
    public float getOffset(float time) {
        return time * time * time;
    }

    /**
     * Linearly interpolate between 2 colors.
     *
     * @param a Color a (when x = 0).
     * @param b Color b (when x = 1).
     * @param x Resulting color distribution.
     * @return  New color which is linearly interpolated between a and b.
     */
    public float[] lerpColor(float[] result, float[] a, float[] b, float x) {
        clamp01(x);
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * (1.0f - x) + b[i] * x;
        }
        return result;
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

    public void setBackgroundSpeed(float speed) {
        backgroundSpeed = speed;
    }
}
