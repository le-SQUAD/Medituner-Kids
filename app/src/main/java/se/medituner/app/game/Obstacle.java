package se.medituner.app.game;

import android.opengl.Matrix;

/**
 * A wrapper class around Quad.
 */
public class Obstacle {

    private Quad quad;
    private float cachedSin, cachedCos;
    private int textureHandle;

    public long creationTime;

    public static float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static float screenRatio;

    private static float scaleMatrix[] = new float[16], translateMatrix[] = new float[16];
    private static float transformMatrix[] = new float[16];

    public Obstacle(Quad quad) {
        this.quad = quad;
        creationTime = 0;
    }

    public static void setScreenRatio(float ratio) {
        screenRatio = ratio;
    }

    public void set(float angle, int textureHandle, long creationTime) {
        cachedCos = (float) Math.cos(angle);
        cachedSin = (float) Math.sin(angle);
        this.textureHandle = textureHandle;
        this.creationTime = creationTime;
    }

    public void draw(float offset) {
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0,
                offset, offset * screenRatio, 1.0f);

        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0,
                offset * cachedCos, offset * cachedSin, 0.0f);

        Matrix.multiplyMM(transformMatrix, 0, translateMatrix, 0, scaleMatrix, 0);
        quad.draw(color, transformMatrix, textureHandle);
    }
}
