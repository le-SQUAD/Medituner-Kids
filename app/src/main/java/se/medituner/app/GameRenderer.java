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
    // Shader program handle.
    private int hProgram;

    private Triangle mTriangle;
    public volatile float mAngle;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    // Stored array to avoid creating additional variables
    private long times[];

    private int FLIP_FACTOR = -1;

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

        // initialize a triangle
        mTriangle = new Triangle();

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

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
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
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
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

        Matrix.setIdentityM(mModelMatrix, 0);
        //touch should toggle bw pos and neg x
        Matrix.scaleM(mModelMatrix, 0, FLIP_FACTOR, 1.0f, 1.0f);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Draw triangle
        mTriangle.draw(mMVPMatrix);

    }

    public void flip(){
        FLIP_FACTOR = FLIP_FACTOR / -1;
    }
}
