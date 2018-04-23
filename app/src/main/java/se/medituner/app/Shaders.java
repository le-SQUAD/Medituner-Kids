package se.medituner.app;

import android.opengl.GLES20;

public class Shaders {

    public static final String POSITION_NAME = "vPosition";
    public static final String TRANSFORM_MATRIX_NAME = "transformMatrix";
    public static final String COLOR_NAME = "vColor";
    public static final String UV_NAME = "vUV";

    static final String SHAPE_VERTEX =
            "uniform mat4 " + TRANSFORM_MATRIX_NAME + ";" +
            "attribute vec2 " + POSITION_NAME + ";" +
            "attribute vec2 " + UV_NAME + ";" +

            "varying vec2 texUV;" +
            "void main() {" +
                "texUV = " + UV_NAME + ";" +
                "gl_Position = " + TRANSFORM_MATRIX_NAME + " * vec4(" + POSITION_NAME + ", 0, 1);" +
            "}";

    static final String SHAPE_FRAGMENT =
            "precision mediump float;" +
            //"uniform "
            "uniform vec4 " + COLOR_NAME + ";" +
            "varying vec2 texUV;" +
            "void main() {" +
                "vec2 pos = (texUV - vec2(0.5, 0.5)) * vec2(2, 2);" +
                "float radius = 1.0 - sqrt(pos.x * pos.x + pos.y * pos.y);" +
                "gl_FragColor = radius * " + COLOR_NAME + ";" +
                //"gl_FragColor = " + COLOR_NAME + ";" +
            "}";

    static final String BACKGROUND_VERTEX =
            "attribute vec2 " + POSITION_NAME + ";" +
            "attribute vec2 " + UV_NAME + ";" +

            "varying vec2 texUV;" +
            "void main() {" +
                "texUV = " + UV_NAME + ";" +
                "gl_Position = vec4(" + POSITION_NAME + ", 0.0, 1.0);" +
            "}";

    static final String BACKGROUND_FRAGMENT =
            "precision mediump float;" +
            "uniform vec4 vColor_Outer;" +
            "uniform vec4 vColor_Inner;" +
            "uniform float fOffset;" +
            "uniform float fRatio;" +

            "varying vec2 texUV;" +

            /*
            "float pingPong(float x) {" +
                "x = mod(x, 2.0);" +
                "if (x > 1.0) {" +
                    "return 2.0 - x;" +
                "} else {" +
                    "return x;" +
                "}" +
            "}" +
            */

            "float pingPong(float x) {" +
                "return (1.0 + sin(x)) / 2.0;" +
            "}" +

            "void main() {" +
                "vec2 pos = (texUV - vec2(0.5, 0.5)) * vec2(2.0, 2.0 / fRatio);" +
                "float radius = sqrt(pos.x * pos.x + pos.y * pos.y) - fOffset;" +

                "gl_FragColor = mix(vColor_Inner, vColor_Outer, pingPong(radius));" +
            "}";

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
