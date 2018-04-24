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

        // Colors of the inner, outer and strip loops
        "uniform vec4 vColor_Outer;" +
        "uniform vec4 vColor_Strip;" +
        "uniform vec4 vColor_Inner;" +
        // We expect a 0 to 1 linear time for the illusion to work.
        "uniform float fTime;" +
        "uniform float fRatio;" +

        "varying vec2 texUV;" +

        //"const float PI = 3.1415926535897932384626433832795;" +
        "const float PIx2 = 6.28318530718;" +

        // Create an offset from given value
        "float offset(float x) {" +
            "return 0.5 / x;" +
        "}" +

        // Shift a -1 to 1 ranged value to 0 to 1 ranged one.
        "float shift(float x) {" +
            "return (1.0 + x) / 2.0;" +
        "}" +

        // The strip mixing value function
        "float mixFunc(float x) {" +
            "return sin(offset(x) + fTime * PIx2);" +
        "}" +

        "vec3 color(float radius) {" +
            "return mix(" +
                "mix(vColor_Inner.rgb, vColor_Outer.rgb, radius)," +
                "vColor_Strip.rgb," +
                "pow(shift(mixFunc(radius)), 0.24)" +
            ") * clamp(0.5 + sqrt(radius), 0.0, 1.0);" +
        "}" +

        "void main() {" +
            "vec2 pos = (texUV - 0.5) * vec2(2.0 * fRatio, 2.0);" +
            "float radius = (pos.x * pos.x + pos.y * pos.y);" +

            "gl_FragColor = vec4(color(radius), 1);" +
        "}";

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
