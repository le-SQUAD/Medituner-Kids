package se.medituner.app.game;

/**
 * The scene interface that houses methods used by the background and drawable objects.
 *
 * @author Grigory Glukhov
 */
public interface IScene {

    /**
     * Get offset for a given lifespan
     *
     * @param time  Lifetime to translate to offset
     * @return      The offset to be used for position and scale.
     */
    float getOffset(float time);
}
