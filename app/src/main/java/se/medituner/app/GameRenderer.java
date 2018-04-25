package se.medituner.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private static long MS_ANIMATION_PERIOD = 3000l;
    private static double ANIMATION_SCALE_POWER = 4.0;
    private static float ANIMATION_SCALE = 1.5f;
    private static float MIN_ORBIT = 0.6f;
    private static float MAX_ORBIT = 0.8f;
    private static float MIN_SUN_SCALE = 0.75f, MAX_SUN_SCALE = 1.5f;
    private static float MIN_PLANET_SCALE = 0.1f, MAX_PLANET_SCALE = 0.3f;

    // A drawable shape reference
    private Shape exampleShape;
    // Transformation matrix for rendering
    private float transformationMatrix[] = new float[16];
    private float scaleMatrix[] = new float[16];
    private float translateMatrix[] = new float[16];
    private float ratio;

    private float lastTime;
    private float angles[] = new float[6];
    private float orbits[] = new float[6];
    private float scales[] = new float[7];

    private Random rng;

    // Shader program handle.
    private int hShapeProgram;
    private int hBackgroundProgram;
    //TEST
    private int mProgram;
    private Obstacle mObstacle;
    // Stored array to avoid creating additional variables
    private long times[];

    /*
    The array of colors that are taken by the shapes.
    The outside array can be arbitrary large, however
    The inside array should always be [4] long (or more, but the other numbers won't be used)
    */
    private float colors[][] = {
            { 0.0f, 0.0f, 1.0f, 1.0f },
            { 0.0f, 1.0f, 0.0f, 1.0f },
            { 0.0f, 1.0f, 1.0f, 1.0f },
            { 1.0f, 0.0f, 0.0f, 1.0f },
            { 1.0f, 0.0f, 1.0f, 1.0f },
            { 1.0f, 1.0f, 0.0f, 1.0f },
            { 1.0f, 1.0f, 1.0f, 1.0f }
    };

    /**
     * Called once when the surface is created during initialization.
     *
     * @param gl        GL context
     * @param config    GL configuration
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        System.out.println("Creating GL surface");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        int vertexShader = Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.SHAPE_VERTEX);
        int fragmentShader = Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.SHAPE_FRAGMENT);

        // Create a new shader program
        hShapeProgram = GLES20.glCreateProgram();

        // Attach shaders to the program
        GLES20.glAttachShader(hShapeProgram, vertexShader);
        GLES20.glAttachShader(hShapeProgram, fragmentShader);

        // Compile the shader program
        GLES20.glLinkProgram(hShapeProgram);

        // Activate the shader program for rendering.
        GLES20.glUseProgram(hShapeProgram);

        exampleShape = Shape.generateQuad(hShapeProgram);
        // TEST
        mObstacle = new Obstacle();

        hBackgroundProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(hBackgroundProgram, Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.BACKGROUND_VERTEX));
        GLES20.glAttachShader(hBackgroundProgram, Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.BACKGROUND_FRAGMENT));

        GLES20.glLinkProgram(hBackgroundProgram);

        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        times = new long[colors.length];
        lastTime = 2.0f;

        rng = new Random();
    }

    /**
     * Called when the surface is resized.
     *
     * @param gl        GL context
     * @param width     new width of the surface
     * @param height    new height of the surface
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        ratio = width / (float) height;
        Background.getInstance(hBackgroundProgram).resize(width, height);
    }

    /**
     * Called each frame, this is where the bulk of the operations should happen.
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //Background background = Background.getInstance(hBackgroundProgram);
        GLES20.glUseProgram(hBackgroundProgram);
        float time = SystemClock.uptimeMillis() % MS_ANIMATION_PERIOD / (float) MS_ANIMATION_PERIOD;
        //background.draw(colors[0], colors[5], time);
        // TEST
        //Obstacle mObstacle = Obstacle.getInstance();
        mObstacle.draw();
    }
}
