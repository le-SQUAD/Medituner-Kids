package se.medituner.app;

import android.opengl.GLES20;
import android.provider.Settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Obstacle {

    /**
     * Create obstacle moving in an angle across the GameScreen
     *
     * @author Vendela Vlk
     */
    //private static Obstacle instance = null;
    // We should have set Life (0-x) of object animation
    private float angle;

    private int mProgram, mPositionHandle, mColorHandle;

    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // For when we should transform object when rendering
    private FloatBuffer vertexBuffer, uvBuffer;
    // In what order vertexes should be drawn
    private ShortBuffer drawListBuffer;
    // Counterclockwise
    static final float obstacleCoords[] = {
            0.0f, 0.5f, 0.0f, // Top
            -0.5f, 0.0f, 0.0f, // Bottom left
            0.5f, 0.0f, 0.0f // Bottom right
    };
    // probs wrong af
    /*static final float obstacleUV[] = {
            0.0f, 0.0f, 0.0f,
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f
    };*/
    float color[] = { 0.5f, 0,5f, 0,5f, 1.0f };
    //static final short obstacle_draw_order[] = {0, 1, 2, 0};
    private final int vertexCount = obstacleCoords.length / COORDS_PER_VERTEX;

    // Create an obstacle at position
    public Obstacle() {

        ByteBuffer bb = ByteBuffer.allocateDirect(obstacleCoords.length * 4);
        // We'll use the hardware's native order
        // Counterclockwise
        bb.order(ByteOrder.nativeOrder());
        // Floating point buffer
        vertexBuffer = bb.asFloatBuffer();
        // Add coordinates
        vertexBuffer.put(obstacleCoords);
        vertexBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        // Empty OpenGL ES program
        mProgram = GLES20.glCreateProgram();
        // Add both vertex and fragment to program
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        // Creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
        // Now we are ready to call draw
        // I need to make my own
        // get handle to vertex shader's vPosition
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    }
    // Rendering shape of obstacle
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    " gl_Position = vPosition;" +
                    "}";
    // Rendering face of shape with colors of obstacle
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    " gl_FragColor = vColor;" +
                    "}";

    public static int loadShader(int type, String shaderCode) {
        // Create a vertex shader type or fragment
        int shader = GLES20.glCreateShader(type);
        // Add the source code to shader and compile
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    public void draw() {
        // Add this program to environment
        GLES20.glUseProgram(mProgram);
        // Enable a handle to obstacle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare obstacle coord data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                                    false, vertexStride, vertexBuffer);
        // Set color for drawing obstacle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        // Drawing
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /*
    public static Obstacle getInstance() {
        if (instance == null)
            instance = new Obstacle();
        return instance;
    }*/
}
