package se.medituner.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private int program;

    private static long MS_HALF_ANIMATION_PERIOD = 1500l;

    static final int COORDS_PER_VERTEX = 3;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    static final float SQUARE_VERTICIES[] = {
            -0.5f, 0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f, 0.5f, 0.0f,   // top right
    };
    static final short SQUARE_DRAW_LIST[] = {0, 1, 2, 0, 2, 3};
    static final int VERTEX_COUNT = SQUARE_VERTICIES.length / COORDS_PER_VERTEX;
    static final float COLOR[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    private float transformationMatrix[] = new float[16];


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

        ByteBuffer bb = ByteBuffer.allocateDirect(SQUARE_VERTICIES.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SQUARE_VERTICIES);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(SQUARE_DRAW_LIST.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(SQUARE_DRAW_LIST);
        drawListBuffer.position(0);

        int vertexShader = Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.VERTEX);
        int fragmentShader = Shaders.loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.FRAGMENT);

        program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        GLES20.glUseProgram(program);
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

        int posHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);


        long time = SystemClock.uptimeMillis() % (2 * MS_HALF_ANIMATION_PERIOD);
        float scale = time / (float) MS_HALF_ANIMATION_PERIOD;
        Matrix.setIdentityM(transformationMatrix, 0);
        Matrix.scaleM(transformationMatrix, 0, scale, scale, scale);

        int matrixHandle = GLES20.glGetUniformLocation(program, "transformMatrix");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, transformationMatrix, 0);

        scale = time >= MS_HALF_ANIMATION_PERIOD ? 2.0f - scale : scale;

        float color[] = { scale, scale, scale, 1.0f};
        // Set color for drawing the triangle
        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                SQUARE_DRAW_LIST.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(posHandle);


        GLES20.glDrawElements(GLES20.GL_TRIANGLES, SQUARE_DRAW_LIST.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }
}
