package se.medituner.app.game;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A quad (rectangle) drawable object.
 *
 */
public class Quad {

    static final int COORDS_PER_VERTEX = 2;
    static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    static final float QUAD_UVS[] = {
        0.0f, 1.0f, // Top left
        0.0f, 0.0f, // Bottom left
        1.0f, 0.0f, // Bottom right
        1.0f, 1.0f  // Top right
    };
    static final short QUAD_DRAW_LIST[] = {0, 1, 2, 0, 2, 3};

    // Buffer of vertex coordinates.
    private FloatBuffer positionBuffer, uvBuffer;
    // Buffer of vertex ids in order that they should be drawn.
    private ShortBuffer drawListBuffer;
    // Handlers to program and position offsets for shape shader.
    private int hProgram, hPosition, hColor, hUV, hTransformMatrix, hTextureOffset;

    /**
     * Create a drawable shape.
     *
     * @param programHandle The handle to shader program that will be used by the shape.
     * @param vertices     Reference vertex array
     * @param draw_order    Reference draw order index array
     */
    public Quad(int programHandle, float width, float height) {
        final float halfWidth = width / 2.0f;
        final float halfHeight = height / 2.0f;
        final float positions[] = {
            -halfWidth, halfHeight,     // Top left
            -halfWidth, -halfHeight,    // Bottom left
            halfWidth, -halfHeight,     // Bottom right
            halfWidth, halfHeight       // Top right
        };

        // Create vertex GL buffer and put java floats into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        positionBuffer = bb.asFloatBuffer();
        positionBuffer.put(positions);
        positionBuffer.position(0);

        // Create a uv buffer
        bb = ByteBuffer.allocateDirect(QUAD_UVS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(QUAD_UVS);
        uvBuffer.position(0);

        // Create vertex-id buffer and put java shorts into it.
        bb = ByteBuffer.allocateDirect(QUAD_DRAW_LIST.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(QUAD_DRAW_LIST);
        drawListBuffer.position(0);

        // Get handles to parts of the shader.
        hProgram = programHandle;
        hPosition = GLES20.glGetAttribLocation(hProgram, Shaders.POSITION_NAME);
        hUV = GLES20.glGetAttribLocation(hProgram, Shaders.UV_NAME);
        hTransformMatrix = GLES20.glGetUniformLocation(hProgram, Shaders.TRANSFORM_MATRIX_NAME);
        hColor = GLES20.glGetUniformLocation(hProgram, Shaders.COLOR_NAME);
        hTextureOffset = GLES20.glGetUniformLocation(hProgram, "sTexture2D");
    }

    /**
     * Draw the shape with provided color and using provided matrix.
     *
     * @param color     A color of the form { float, float, float, float}
     * @param matrix    A 4x4 transformation matrix for the shape.
     * @param textureHandle
     */
    public void draw(float[] color, float[] matrix, int textureHandle) {
        // Set the next vertex-array appointment to the 'position' offset in the shader.
        GLES20.glEnableVertexAttribArray(hPosition);
        GLES20.glEnableVertexAttribArray(hUV);

        // Supply the vertices for the 'position' in the shader.
        GLES20.glVertexAttribPointer(hPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, positionBuffer);

        GLES20.glVertexAttribPointer(hUV, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, uvBuffer);

        // Supply the matrix for the transformation matrix
        GLES20.glUniformMatrix4fv(hTransformMatrix, 1, false, matrix, 0);

        // Supply color for fragment shader
        GLES20.glUniform4fv(hColor, 1, color, 0);

        // Set active texture unit to unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(hTextureOffset, 0);

        // Draw the triangles.
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                QUAD_DRAW_LIST.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Clean up after our-selves (potentially unnecessary)
        GLES20.glDisableVertexAttribArray(hPosition);
        GLES20.glDisableVertexAttribArray(hUV);
    }
}