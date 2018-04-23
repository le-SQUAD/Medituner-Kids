package se.medituner.app;

import android.graphics.Shader;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Shape {

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
    private int hProgram, hPosition, hColor, hUV, hTransformMatrix;

    /**
     * Create a drawable shape.
     *
     * @param programHandle The handle to shader program that will be used by the shape.
     * @param vertices     Reference vertex array
     * @param draw_order    Reference draw order index array
     */
    public Shape(int programHandle, float[] vertices, float[] uvs, short[] draw_order) {
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
        hTransformMatrix = GLES20.glGetUniformLocation(hProgram, Shaders.TRANSFORM_MATRIX_NAME);
        hColor = GLES20.glGetUniformLocation(hProgram, Shaders.COLOR_NAME);
    }

    /**
     * Draw the shape with provided color and using provided matrix.
     *
     * @param color     A color of the form { float, float, float, float}
     * @param matrix    A 4x4 transformation matrix for the shape.
     */
    public void draw(float[] color, float[] matrix) {
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

        // Supply the matrix for the transformation matrix
        GLES20.glUniformMatrix4fv(hTransformMatrix, 1, false, matrix, 0);

        // Supply color for fragment shader
        GLES20.glUniform4fv(hColor, 1, color, 0);

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
    public static Shape generateQuad(int programHandle) {
        return new Shape(programHandle, QUAD_VERTICIES, QUAD_UVS, QUAD_DRAW_LIST);
    }
}