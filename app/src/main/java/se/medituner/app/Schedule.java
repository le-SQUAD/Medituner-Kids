package se.medituner.app;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

//MIN HH:mm
//public static final LocalTime MIN;

/**
 * Medication schedule. Handles almost everything schedule-related.
 *
 * @author Aleksandra Soltan, Julia Danek, Grigory Glukhov
 */
public class Schedule implements Serializable {

    // Dependency injection
    private IClock time;
    private transient Streak streak;

    // Schedule pools
    private final Queue<Medication> morningPool;
    private final Queue<Medication> lunchPool;
    private final Queue<Medication> eveningPool;

    // Schedule active queue.
    private Date queueCreationTime;
    private Queue<Medication> activeQueue;

    public Schedule(IClock time) {
        this.time = time;
        morningPool = new LinkedList<>();
        lunchPool = new LinkedList<>();
        eveningPool = new LinkedList<>();
        activeQueue = new LinkedList<>();

        Calendar queueFakeTime = time.now();
        queueFakeTime.setTime(getBeginningOfCurrentPeriod(time));
        queueFakeTime.add(Calendar.MINUTE, -10);
        queueCreationTime = queueFakeTime.getTime();
    }

    public void connectStreak(Streak streak) {
        this.streak = streak;
    }


    /**
     * Switches active queue to the correct pool
     */
    private void updateQueue() {
        Date periodBeginning = getBeginningOfCurrentPeriod(time);
        queueCreationTime = time.now().getTime();
        Calendar cal = time.now();
        cal.setTime(periodBeginning);
        switch (cal.get(Calendar.HOUR_OF_DAY)) {
            case 5:
                activeQueue = new LinkedList<>(morningPool);
                break;

            case 11:
                activeQueue = new LinkedList<>(lunchPool);
                break;

            default:
                activeQueue = new LinkedList<>(eveningPool);
                break;
        }
    }

    /**
     * Checks if the current queue is valid, and updates accordingly.
     *
     * @param updateStreak Should the streak be updated? (incremented or reset)
     * @author Aleksandra Soltan, Grigory Glukhov
     */
    public void validateQueue(boolean updateStreak) {
        if(queueCreationTime.before(getBeginningOfCurrentPeriod(time))){
            if(activeQueue.isEmpty()) {
                if (updateStreak && streak != null) {
                    if (getBeginningOfLastPeriod(time).before(queueCreationTime)) {
                        // The queue is from the previous period
                        streak.increment();
                    } else {
                        // The queue is not from previous period, so reset the streak
                        streak.reset();
                    }
                }
                updateQueue();
            } else {
                if (updateStreak && streak != null)
                    streak.reset();
            }
        }

    }


    /**
     * Get the beginning of the current period (morningPool, lunchPool or eveningPool).
     * The returned date is back in time relative to time.now()
     *
     * @param time IClock interface for now() method.
     * @return Date corresponding to the beginning of the current period (back in time).
     * @author Aleksandra Soltan, Grigory Glukhov
     */
    public static Date getBeginningOfCurrentPeriod(IClock time) {
        Calendar now = time.now();
        Calendar comparison = time.now();
        comparison.set(Calendar.MINUTE, 0);
        comparison.set(Calendar.SECOND, 0);

        comparison.set(Calendar.HOUR_OF_DAY, 5);
        if (now.getTime().before(comparison.getTime())) {
            comparison.add(Calendar.HOUR_OF_DAY, -12);
            return comparison.getTime();
        } else {
            comparison.set(Calendar.HOUR_OF_DAY, 11);
            if (now.getTime().before(comparison.getTime())) {
                comparison.set(Calendar.HOUR_OF_DAY, 5);
                return comparison.getTime();
            } else {
                comparison.set(Calendar.HOUR_OF_DAY, 17);
                if (now.getTime().before(comparison.getTime())) {
                    comparison.set(Calendar.HOUR_OF_DAY, 11);
                    return comparison.getTime();
                } else {
                    return comparison.getTime();
                }
            }
        }
    }

    /**
     * Get the beginning of the previous period (in the past, before current period beginning).
     *
     * @param time IClock interface for now() method.
     * @return The Date of the beginning of the previous period.
     * @author Grigory Glukhov
     */
    public static Date getBeginningOfLastPeriod(IClock time) {
        Calendar calendar = time.now();
        calendar.setTime(getBeginningOfCurrentPeriod(time));
        switch (calendar.get(Calendar.HOUR_OF_DAY)) {
            case 5:
                calendar.add(Calendar.HOUR_OF_DAY, -12);
                break;

            case 11:
                calendar.set(Calendar.HOUR_OF_DAY, 5);
                break;

            default:
                calendar.set(Calendar.HOUR_OF_DAY, 17);
                break;
        }
        return calendar.getTime();
    }


    /**
     * Get the beginning of the following period.
     *
     * @param time IClock interface for now() method.
     * @return Date corresponding to the beginning of the next period (future).
     */
    public static Date getBeginningOfNextPeriod(IClock time) {
        Calendar now = time.now();
        Calendar comparison = time.now();
        comparison.set(Calendar.MINUTE, 0);
        comparison.set(Calendar.SECOND, 0);

        comparison.set(Calendar.HOUR_OF_DAY, 5);
        if (now.getTime().before(comparison.getTime())) {
            return comparison.getTime();
        } else {
            comparison.set(Calendar.HOUR_OF_DAY, 11);
            if (now.getTime().before(comparison.getTime())) {
                return comparison.getTime();
            } else {
                comparison.set(Calendar.HOUR_OF_DAY, 17);
                if (now.getTime().before(comparison.getTime())) {
                    return comparison.getTime();
                } else {
                    comparison.add(Calendar.HOUR_OF_DAY, 12);
                    return comparison.getTime();
                }
            }
        }
    }

    public Queue getActiveQueue(){
          return activeQueue;
    }

    public void purgeMorningPool() {
        while(!morningPool.isEmpty()) {
            morningPool.remove();
        }
    }

    public void purgeLunchPool() {
        while(!lunchPool.isEmpty()) {
            lunchPool.remove();
        }
    }

    public void purgeEveningPool() {
        while(!eveningPool.isEmpty()) {
            eveningPool.remove();
        }
    }

    public void addMedToMorningPool(Medication med) {
        morningPool.add(med);
    }

    public void addMedToLunchPool(Medication med) {
        lunchPool.add(med);
    }

    public void addMedToEveningPool(Medication med) {
        eveningPool.add(med);
    }
}
