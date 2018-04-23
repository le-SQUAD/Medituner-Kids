package se.medituner.app;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Background {

    private static Background instance = null;

    static final int COORDS_PER_VERTEX = 2;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    static final float QUAD_VERTICIES[] = {
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
    static final int QUAD_VERTEX_COUNT = QUAD_VERTICIES.length / COORDS_PER_VERTEX;

    // Buffer of vertex coordinates.
    private FloatBuffer vertexBuffer, uvBuffer;
    // Buffer of vertex ids in order that they should be drawn.
    private ShortBuffer drawListBuffer;
    // Handlers to program and position offsets for shape shader.
    private int hProgram, hPosition, hInner, hOuter, hUV, hRatio, hOffset;
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
        hInner = GLES20.glGetUniformLocation(hProgram, "vColor_Inner");
        hOuter = GLES20.glGetUniformLocation(hProgram, "vColor_Outer");
        hOffset = GLES20.glGetUniformLocation(hProgram, "fOffset");
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
     * @param innerColor    Color A
     * @param outerColor    Color B
     * @param offset        offset between the colors
     */
    public void draw(float[] innerColor, float[] outerColor, float offset) {
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

        GLES20.glUniform4fv(hInner, 1, innerColor, 0);
        GLES20.glUniform4fv(hOuter, 1, outerColor, 0);
        GLES20.glUniform1f(hRatio, ratio);
        GLES20.glUniform1f(hOffset, offset);

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
            instance = new Background(programHandle, QUAD_VERTICIES, QUAD_UVS, QUAD_DRAW_LIST);
        return instance;
    }
}
