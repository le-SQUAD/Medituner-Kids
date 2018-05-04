package se.medituner.app.game;

import android.opengl.Matrix;

/**
 * A wrapper class around Quad.
 *
 * @author Vendela Vlk, Grigory Glukhov
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

    /**
     * Set the canvas screen y to x ratio. Used for implicit projection matrix calculation.
     *
     * @param ratio The ratio of the screen.
     */
    public static void setScreenRatio(float ratio) {
        screenRatio = ratio;
        inverseRatio = 1.0f / ratio;
    }

    /**
     * Set new variables for the Obstacle
     *
     * @param angle         The new angle of the obstacle.
     * @param textureHandle The handle to texture to be used for drawing the obstacle.
     * @param creationTime  The time of creation of the obstacle.
     * @param lane          The lane of the obstacle.
     */
    public void set(float angle, int textureHandle, long creationTime, Lane lane) {
        cachedCos = (float) Math.cos(angle);
        cachedSin = (float) Math.sin(angle);
        this.textureHandle = textureHandle;
        this.creationTime = creationTime;
        this.lane = lane;
    }

    /**
     * Draw the Obstacle with given offset.
     *
     * @param offset Offset parameter.
     */
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
