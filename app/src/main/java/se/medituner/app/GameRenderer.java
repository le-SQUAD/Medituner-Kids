package se.medituner.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private static long MS_ANIMATION_PERIOD = 4000l;
    private static double ANIMATION_SCALE_POWER = 4.0;

    // A drawable shape reference
    private Shape exampleShape;
    // Transformation matrix for rendering
    private float transformationMatrix[] = new float[16];
    private float scaleMatrix[] = new float[16];
    private float translateMatrix[] = new float[16];
    private float ratio;
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

        exampleShape = Shape.generateSquare(hProgram);

        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        times = new long[colors.length];
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

        Matrix.setIdentityM(transformationMatrix, 0);
        Matrix.scaleM(transformationMatrix, 0, scale, scale * ratio, 1.0f);
        exampleShape.draw(colors[6], transformationMatrix);

        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, 0.2f * scale, 0.2f * scale * ratio, 1.0f);
        Matrix.translateM(translateMatrix, 0, 0.0f, -scale * ratio, 0.0f);
        Matrix.multiplyMM(transformationMatrix, 0,
                translateMatrix, 0,
                scaleMatrix, 0);
        exampleShape.draw(colors[3], transformationMatrix);
    }
}
