package se.medituner.app;

import android.opengl.GLES20;

public class Shaders {

    public static final String POSITION_NAME = "vPosition";
    public static final String TRANSFORM_MATRIX_NAME = "transformMatrix";
    public static final String COLOR_NAME = "vColor";

    static final String SHAPE_VERTEX =
            "uniform mat4 " + TRANSFORM_MATRIX_NAME + ";" +
            "attribute vec3 " + POSITION_NAME + ";" +
            "void main() {" +
            "   gl_Position = " + TRANSFORM_MATRIX_NAME + " * vec4(" + POSITION_NAME + ", 1);" +
            "}";

    static final String SHAPE_FRAGMENT =
            "precision mediump float;" +
            "uniform vec4 " + COLOR_NAME + ";" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
