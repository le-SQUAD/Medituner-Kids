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
    private int hProgram;

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
        hProgram = GLES20.glCreateProgram();

        // Attach shaders to the program
        GLES20.glAttachShader(hProgram, vertexShader);
        GLES20.glAttachShader(hProgram, fragmentShader);

        // Compile the shader program
        GLES20.glLinkProgram(hProgram);

        // Activate the shader program for rendering.
        GLES20.glUseProgram(hProgram);

        exampleShape = Shape.generateQuad(hProgram);

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
    }

    /**
     * Called each frame, this is where the bulk of the operations should happen.
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear the surface
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        float time = (SystemClock.uptimeMillis() % MS_ANIMATION_PERIOD) / (float) MS_ANIMATION_PERIOD;
        //float scale = (float) Math.pow(time, ANIMATION_SCALE_POWER);
        float scale = time * time * time;

        if (time < lastTime) {
            for (int i = 0; i < angles.length; i++) {
                angles[i] = rng.nextFloat() * (float) Math.PI * 2.0f;
                orbits[i] = MIN_ORBIT + rng.nextFloat() * (MAX_ORBIT - MIN_ORBIT);
                scales[i] = MIN_PLANET_SCALE + rng.nextFloat() * (MAX_PLANET_SCALE - MIN_PLANET_SCALE);
            }
            scales[6] = MIN_SUN_SCALE + rng.nextFloat() * (MAX_SUN_SCALE - MIN_SUN_SCALE);
        }
        lastTime = time;
        float alpha = (float) Math.sin(time * (float) Math.PI);

        Matrix.setIdentityM(transformationMatrix, 0);
        Matrix.scaleM(transformationMatrix, 0,
                ANIMATION_SCALE * scale * scales[6], ANIMATION_SCALE * scale * scales[6] * ratio, 1.0f);
        colors[6][3] = alpha;
        exampleShape.draw(colors[6], transformationMatrix);

        for (int i = 0; i < angles.length; i++) {
            Matrix.setIdentityM(scaleMatrix, 0);
            Matrix.setIdentityM(translateMatrix, 0);
            Matrix.scaleM(scaleMatrix, 0,
                    ANIMATION_SCALE * scale * scales[i] , ANIMATION_SCALE * scale * scales[i] * ratio, 1.0f);
            Matrix.translateM(translateMatrix, 0,
                    scale * (float) Math.cos(angles[i]) * orbits[i],
                    scale * (float) Math.sin(angles[i]) * orbits[i] * ratio, 0.0f);
            Matrix.multiplyMM(transformationMatrix, 0,
                    translateMatrix, 0,
                    scaleMatrix, 0);
            colors[i][3] = alpha;
            exampleShape.draw(colors[i], transformationMatrix);
        }
    }
}
