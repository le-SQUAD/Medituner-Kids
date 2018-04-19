package se.medituner.app;

import java.io.Serializable;

/**
 * Reward-streak class, responsible for keeping track of the complete schedule streak.
 *
 * @author Sasa Lekic, Julia Danek, Grigory Glukhov
 */
public class Streak implements Serializable {

    public interface ChangeListener {

        void onStreakChanged(int newStreak);
    }

    private transient ChangeListener listener = null;
    private int streak;

    public Streak() {}

    public Streak(ChangeListener listener) {
        this.listener = listener;
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public void increment() {
        streak++;
        if (listener != null)
            listener.onStreakChanged(streak);
    }

    public void reset() {
        streak = 0;
        if (listener != null)
            listener.onStreakChanged(streak);
    }

    public int getValue() {
        return streak;
    }
}
