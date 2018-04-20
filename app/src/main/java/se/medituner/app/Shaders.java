package se.medituner.app;

import android.opengl.GLES20;

public class Shaders {

    static final String VERTEX =
            "uniform mat4 transformMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "   gl_Position = vPosition * transformMatrix;" +
            "}";

    static final String FRAGMENT =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
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
