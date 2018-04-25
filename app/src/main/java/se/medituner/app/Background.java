package se.medituner.app;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import se.medituner.app.game.Shaders;

public class Background {

    private static Background instance = null;

    static final int COORDS_PER_VERTEX = 2;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    static final float QUAD_POSITIONS[] = {
            -1.0f, 1.0f,  // top left
            -1.0f, -1.0f, // bottom left
            1.0f, -1.0f,  // bottom right
            1.0f, 1.0f,   // top right
    };
    static final float QUAD_UVS[] = {
            0.0f, 1.0f, // Top left
            0.0f, 0.0f, // Bottom left
            1.0f, 0.0f, // Bottom right
            1.0f, 1.0f  // Top right
    };
    static final short QUAD_DRAW_LIST[] = {0, 1, 2, 0, 2, 3};

    // Buffer of vertex coordinates.
    private FloatBuffer vertexBuffer, uvBuffer;
    // Buffer of vertex ids in order that they should be drawn.
    private ShortBuffer drawListBuffer;
    // Handlers to program and position offsets for the background shader.
    private int hProgram, hPosition, hUV;
    // Handlers to color offsets for the background shader.
    private int hColors[];
    // Handlers to time and screen ratio offsets for the background shader.
    private int hTime, hRatio;
    private float ratio;

    /**
     * Create a drawable shape.
     *
     * @param programHandle The handle to shader program that will be used by the shape.
     * @param vertices     Reference vertex array
     * @param draw_order    Reference draw order index array
     */
    private Background(int programHandle, float[] vertices, float[] uvs, short[] draw_order) {
        // Create vertex GL buffer and put java floats into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // Create a uv buffer
        bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Create vertex-id buffer and put java shorts into it.
        bb = ByteBuffer.allocateDirect(draw_order.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(draw_order);
        drawListBuffer.position(0);

        // Get handles to parts of the shader.
        hProgram = programHandle;
        hPosition = GLES20.glGetAttribLocation(hProgram, Shaders.POSITION_NAME);
        hUV = GLES20.glGetAttribLocation(hProgram, Shaders.UV_NAME);
        hColors = new int[4];
        hColors[0] = GLES20.glGetUniformLocation(hProgram, "vColor0");
        hColors[1] = GLES20.glGetUniformLocation(hProgram, "vColor1");
        hColors[2] = GLES20.glGetUniformLocation(hProgram, "vColor2");
        hColors[3] = GLES20.glGetUniformLocation(hProgram, "vColor3");
        hTime = GLES20.glGetUniformLocation(hProgram, "fTime");
        hRatio = GLES20.glGetUniformLocation(hProgram, "fRatio");
    }

    /**
     * Resize the internal size parameters for corrent ratio rendering.
     *
     * @param width
     * @param height
     */
    public void resize(int width, int height) {
        ratio = width / (float) height;
    }

    /**
     * Draw the shape with provided color and using provided matrix.
     *
     * @param innerColor    The color on the inside (the center of the screen).
     * @param stripColor    The strip (darker) color.
     * @param outerColor    The color on the outside (towards the edges of the screen).
     * @param time          Linear increasing time for the animation, between 0 and 1.
     */
    public void draw(float[][] colors, float time) {
        // Set the next vertex-array appointment to the 'position' offset in the shader.
        GLES20.glEnableVertexAttribArray(hPosition);
        GLES20.glEnableVertexAttribArray(hUV);

        // Supply the vertices for the 'position' in the shader.
        GLES20.glVertexAttribPointer(hPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);

        GLES20.glVertexAttribPointer(hUV, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, uvBuffer);


        for (int i = 0; i < hColors.length; i++)
            GLES20.glUniform3fv(hColors[i], 1, colors[i], 0);
        GLES20.glUniform1f(hRatio, ratio);
        GLES20.glUniform1f(hTime, time);

        // Draw the triangles.
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                QUAD_DRAW_LIST.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Clean up after our-selves (potentially unnecessary)
        GLES20.glDisableVertexAttribArray(hPosition);
        GLES20.glDisableVertexAttribArray(hUV);
    }

    /**
     * Generate a basic square shape
     *
     * @param programHandle Handle to the program shader to be used by the shape.
     * @return A square shape.
     */
    public static Background getInstance(int programHandle) {
        if (instance == null)
            instance = new Background(programHandle, QUAD_POSITIONS, QUAD_UVS, QUAD_DRAW_LIST);
        return instance;
    }
}
