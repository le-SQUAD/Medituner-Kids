package se.medituner.app;

import android.opengl.GLES20;

public class Shaders {

    public static final String POSITION_NAME = "vPosition";
    public static final String TRANSFORM_MATRIX_NAME = "transformMatrix";
    public static final String COLOR_NAME = "vColor";
    public static final String UV_NAME = "vUV";

    static final String QUAD_VERTEX =
            "uniform mat4 " + TRANSFORM_MATRIX_NAME + ";" +
            "attribute vec2 " + POSITION_NAME + ";" +
            "attribute vec2 " + UV_NAME + ";" +

            "varying vec2 texUV;" +
            "void main() {" +
                "texUV = " + UV_NAME + ";" +
                "gl_Position = " + TRANSFORM_MATRIX_NAME + " * vec4(" + POSITION_NAME + ", 0, 1);" +
            "}";

    static final String QUAD_FRAGMENT =
            "precision mediump float;" +

            "uniform sampler2D sTexture;" +
            "uniform vec4 " + COLOR_NAME + ";" +

            "varying vec2 texUV;" +

            "void main() {" +
                "gl_FragColor = texture2D(sTexture, texUV) * " + COLOR_NAME + ";" +
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

        "uniform vec3 vColor0;" +
        "uniform vec3 vColor1;" +
        "uniform vec3 vColor2;" +
        "uniform vec3 vColor3;" +

        // We expect a 0 to 1 linear time for the illusion to work.
        "uniform float fTime;" +
        "uniform float fRatio;" +

        "varying vec2 texUV;" +

        /*
        "const vec3 COLOR_0 = vec3(0.5, 0.0431372549, 0.0431372549)" +
        "const vec3 COLOR_1 = vec3(0.73725490196, 0.34117647058, 0.34117647058);" +
        "const vec3 COLOR_2 = vec3(0.65098039215, 0.19215686274, 0.19215686274);" +
        "const vec3 COLOR_3 = vec3(0.85098039215, 0.5, 0.5);" +
        */

        "const float TAU = 6.28318530718;" +
        "const float GRADIENT_FACTOR = 128.0;" +
        "const float SINGULARITY_OFFSET = 0.044;" +

        // Create an offset from given value
        "float offset(float x) {" +
            "return 1.0 / (x + SINGULARITY_OFFSET);" +
        "}" +

        // Shift a -1 to 1 ranged value to 0 to 1 ranged one.
        "float shift(float x) {" +
            "return (1.0 + x) / 2.0;" +
        "}" +

        // The strip mixing value function
        "float mixFunc(float x) {" +
            "return sin(offset(x) + fTime * TAU);" +
        "}" +

        "vec3 sharpMix(vec3 a, vec3 b, float x, float gf) {" +
            "return mix(a, b, clamp(x * (gf + 1.0) - gf/2.0, 0.0, 1.0));" +
        "}" +

        "float gf(float x) {" +
            "return x * GRADIENT_FACTOR;" +
        "}" +

        "vec3 mix4(vec3 a, vec3 b, vec3 c, vec3 d, float x, float r) {" +
            "x = x * 4.0;" +
            "return sharpMix(" +
                "sharpMix(a, b, (x + 3.0) * 2.0, gf(r))," +
                "sharpMix(c, d, x - 3.0, gf(r))," +
                "x - 1.0, gf(r));" +
        "}" +

        "vec3 color(float radius) {" +
            "return mix4(vColor0, vColor1, vColor2, vColor3, mixFunc(radius), radius);" +
        "}" +

        "void main() {" +
            "vec2 pos = (texUV - 0.5) * vec2(2.0 * fRatio, 2.0);" +
            "float radius = distance(vec2(0.0, 0.0), pos);" +

            "gl_FragColor = vec4(color(radius), 1);" +
        "}";

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
