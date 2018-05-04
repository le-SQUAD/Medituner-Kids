package se.medituner.app.game;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import se.medituner.app.Persistence;

/**
 * HighScore tracking class.
 *
 * @author Agnes
 */
public class HighScore {

    public static final String SAVED_SCORE = "savedScore";
    public static final int MS_SCORE_PERIOD = 500;

    private GameScreen gameScreen;
    private Persistence persistence;
    private Timer timer;
    private TimerTask task;

    private int currentScore = 0, highScore;

    /**
     * Create a new instance of HighScore class.
     *
     * @param gameScreen    The game screen to display the high score on.
     * @param persistence   The persistence class for saving and loading high score.
     */
    public HighScore(GameScreen gameScreen, Persistence persistence) {
        this.gameScreen = gameScreen;
        this.persistence = persistence;
        this.timer = new Timer(true);

        try {
            highScore = (Integer) persistence.loadObject(SAVED_SCORE);
        } catch (IOException e) {
            System.out.println("Failed to load high score, resetting it to 0.");
            highScore = 0;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Begin the high score ticking.
     */
    public void begin() {
        if (task == null) {
            task = new ScoreTimer();
            timer.scheduleAtFixedRate(task, MS_SCORE_PERIOD, MS_SCORE_PERIOD);
        }
    }

    /**
     * Stop the high score ticking.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            timer.purge();
            task = null;
        }
    }

    /**
     * Get the current score.
     *
     * @return Current score.
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Get highest achieved score.
     *
     * @return Highest achieved score.
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * Timer task that is used to increment the score.
     */
    private class ScoreTimer extends TimerTask {
        @Override
        public void run() {
            if (++currentScore > highScore)
                highScore = currentScore;
            gameScreen.updateScoreTexts();
        }
    }

    /**
     * Reset current score, saving it if required.
     */
    public void resetScore() {
        if (currentScore == highScore) {
            try {
                persistence.saveObject(highScore, SAVED_SCORE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentScore = 0;
    }

    public static void resetHighScore(Persistence persistence) {
        try {
            persistence.saveObject((int) 0, SAVED_SCORE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

