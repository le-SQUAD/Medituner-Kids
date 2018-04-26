package se.medituner.app.game;

import android.opengl.Matrix;

/**
 * A wrapper class around Quad.
 */
public class Obstacle {

    private Quad quad;
    private float cachedSin, cachedCos;
    private int textureHandle;

    private static final float FADE_OFFSET = 2.5f;

    public long creationTime;
    public Lane lane;

    public static float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static float screenRatio;
    private static float inverseRatio;

    private static float scaleMatrix[] = new float[16], translateMatrix[] = new float[16];
    private static float transformMatrix[] = new float[16];

    public Obstacle(Quad quad) {
        this.quad = quad;
        creationTime = 0;
    }

    public static void setScreenRatio(float ratio) {
        screenRatio = ratio;
        inverseRatio = 1.0f / ratio;
    }

    public void set(float angle, int textureHandle, long creationTime, Lane lane) {
        cachedCos = (float) Math.cos(angle);
        cachedSin = (float) Math.sin(angle);
        this.textureHandle = textureHandle;
        this.creationTime = creationTime;
        this.lane = lane;
    }

    public void draw(float offset) {
        Matrix.setIdentityM(transformMatrix, 0);
        Matrix.translateM(transformMatrix, 0,
                offset * inverseRatio * cachedCos, offset * inverseRatio * cachedSin, 0.0f);
        Matrix.scaleM(transformMatrix, 0,
                offset, offset * screenRatio, 1.0f);

        color[3] = FADE_OFFSET - (offset * FADE_OFFSET);
        quad.draw(color, transformMatrix, textureHandle);
    }
}
