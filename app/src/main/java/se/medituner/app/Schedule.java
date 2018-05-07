package se.medituner.app;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Medication schedule. Handles almost everything schedule-related.
 *
 * @author Aleksandra Soltan, Julia Danek, Grigory Glukhov
 */
public class Schedule implements Serializable {

    public static final int GENERATOR_MIN_POOL_SIZE = 2;
    public static final int GENERATOR_MAX_POOL_SIZE = 5;

    public static final int PERIOD_BEGINNING_MORNING = 5;
    public static final int PERIOD_BEGINNING_LUNCH = 11;
    public static final int PERIOD_BEGINNING_EVENING = 17;

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
    private boolean streakUpdated = false;

    public Schedule(IClock time) {
        this.time = time;
        morningPool = new LinkedList<>();
        lunchPool = new LinkedList<>();
        eveningPool = new LinkedList<>();
        activeQueue = new LinkedList<>();

        Calendar queueFakeTime = Calendar.getInstance();
        queueFakeTime.setTime(getBeginningOfCurrentPeriod(time));
        queueFakeTime.add(Calendar.MINUTE, -10);
        queueCreationTime = queueFakeTime.getTime();
    }

    public void connectStreak(Streak streak) {
        this.streak = streak;
    }

    /**
     * Switches active queue to the correct pool
     * @author Grigory Glukhov, Aleksandra Soltan
     * @author Agnes Petäjävaara(latest update), added the not taken medication to the current pool
     */
    private void updateQueue() {
        Date periodBeginning = getBeginningOfCurrentPeriod(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(periodBeginning);
        switch (cal.get(Calendar.HOUR_OF_DAY)) {
            //Checks if the all medication the previous day was taken, if so increase the streak and
            //create a new morning pool of medication
            case PERIOD_BEGINNING_MORNING: {
                /*
                if (!activeQueue.isEmpty() || getBeginningOfLastPeriod(time).after(queueCreationTime)) {
                    streak.reset();
                } else {
                    streak.increment();
                }
                */
                    activeQueue = new LinkedList<>(morningPool);
            }

            break;

            case PERIOD_BEGINNING_LUNCH:  {
                if (getBeginningOfLastPeriod(time).before(queueCreationTime)) {
                    // This means activeQueue is semi-valid
                    activeQueue = mergeQueues(activeQueue, lunchPool);
                } else {
                    // We can infer that activeQueue was created before this morning, so its invalid
                    activeQueue = mergeQueues(morningPool, lunchPool);
                }
            } break;

            case PERIOD_BEGINNING_EVENING: {
                if (queueCreationTime.after(getBeginningOfLastPeriod(time))) {
                    activeQueue = mergeQueues(activeQueue, eveningPool);
                } else {
                    Queue<Medication> lunchEveningMeds = mergeQueues(lunchPool, eveningPool);
                    if (queueCreationTime.after(getBeginningOfPreviousPeriod(getBeginningOfLastPeriod(time)))) {
                        activeQueue = mergeQueues(activeQueue, lunchEveningMeds);
                    } else {
                        activeQueue = mergeQueues(morningPool, lunchEveningMeds);
                    }
                }
            } break;

            default:
                throw new IllegalStateException("Unexpected time of day!");
        }
        queueCreationTime = time.now();
    }

    /**
     * Checks if the current queue is valid, and updates accordingly.
     *
     * @param updateStreak  Should the streak be updated? (incremented or reset)
     * @author              Aleksandra Soltan, Grigory Glukhov
     */
    public void validateQueue(boolean updateStreak) {
        Date date = getBeginningOfCurrentPeriod(time);
        if (queueCreationTime.before(getBeginningOfCurrentPeriod(time))) {
            if (updateStreak && streak != null) {
                if (!activeQueue.isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    // Set to this morning
                    calendar.setTime(time.now());
                    calendar.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    if (queueCreationTime.before(calendar.getTime())) {
                        streak.reset();
                    }
                }
            }
            updateQueue();
            streakUpdated = false;
        } else {
            if (activeQueue.isEmpty() && updateStreak
                    && !streakUpdated && streak != null) {
                // Queue is empty, it doesn't need to update,
                // However we want to reward the player immediately
                streakUpdated = true;
                streak.increment();
            }
        }
    }


    /**
     * Get the beginning of the current period (morningPool, lunchPool or eveningPool).
     * The returned date is back in time relative to time.now()
     *
     * @param time  IClock interface for now() method.
     * @return      Date corresponding to the beginning of the current period (back in time).
     * @author      Aleksandra Soltan, Grigory Glukhov
     */
    public static Date getBeginningOfCurrentPeriod(IClock time) {
        Calendar now = Calendar.getInstance();
        now.setTime(time.now());
        Calendar comparison = Calendar.getInstance();
        comparison.setTime(time.now());

        comparison.set(Calendar.MINUTE, 0);
        comparison.set(Calendar.SECOND, 0);
        comparison.set(Calendar.MILLISECOND, 0);

        comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING);
        if (now.getTime().before(comparison.getTime())) {
            comparison.add(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING - PERIOD_BEGINNING_EVENING);
            return comparison.getTime();
        } else {
            comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_LUNCH);
            if (now.getTime().before(comparison.getTime())) {
                comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING);
                return comparison.getTime();
            } else {
                comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_EVENING);
                if (now.getTime().before(comparison.getTime())) {
                    comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_LUNCH);
                    return comparison.getTime();
                } else {
                    return comparison.getTime();
                }
            }
        }
    }

    /**
     * Return beginning of a previous period for a given time.
     *
     * @param time  The beginning of relative current period.
     * @return      The beginning of the period that was before the one that is "now".
     */
    public static Date getBeginningOfPreviousPeriod(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        switch (calendar.get(Calendar.HOUR_OF_DAY)) {
            case PERIOD_BEGINNING_MORNING:
                calendar.add(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING - PERIOD_BEGINNING_EVENING);
                break;

            case PERIOD_BEGINNING_LUNCH:
                calendar.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING);
                break;

            case PERIOD_BEGINNING_EVENING:
                calendar.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_LUNCH);
                break;

            default:
                throw new IllegalStateException("Unexpected time of day!");
        }
        return calendar.getTime();
    }

    /**
     * Get the beginning of the previous period (in the past, before current period beginning).
     *
     * @param time  IClock interface for now() method.
     * @return      The Date of the beginning of the previous period.
     * @author      Grigory Glukhov
     */
    public static Date getBeginningOfLastPeriod(IClock time) {
        return getBeginningOfPreviousPeriod(getBeginningOfCurrentPeriod(time));
    }

    /**
     * Get the beginning of the following period.
     *
     * @param time  IClock interface for now() method.
     * @return      Date corresponding to the beginning of the next period (future).
     */
    public static Date getBeginningOfNextPeriod(IClock time) {
        Calendar now = Calendar.getInstance();
        now.setTime(time.now());
        Calendar comparison = Calendar.getInstance();
        comparison.setTime(time.now());

        comparison.set(Calendar.MINUTE, 0);
        comparison.set(Calendar.SECOND, 0);
        comparison.set(Calendar.MILLISECOND, 0);

        comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_MORNING);
        if (now.getTime().before(comparison.getTime())) {
            return comparison.getTime();
        } else {
            comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_LUNCH);
            if (now.getTime().before(comparison.getTime())) {
                return comparison.getTime();
            } else {
                comparison.set(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_EVENING);
                if (now.getTime().before(comparison.getTime())) {
                    return comparison.getTime();
                } else {
                    comparison.add(Calendar.HOUR_OF_DAY, PERIOD_BEGINNING_EVENING - PERIOD_BEGINNING_MORNING);
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

    /**
     * Reset the active queue to the current period pool.
     *
     * WARNING!
     * This is not functional behavior for the class and the method should only be used for testing!
     */
    public void resetQueue() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time.now());
        calendar.add(Calendar.DATE, -1);
        queueCreationTime = calendar.getTime();
        validateQueue(false);
    }

    /**
     * Generate a new schedule.
     * The new schedule generated will have between GENERATOR_MIN_POOL and GENERATOR_MAX_POOL non missing items in each pool
     *
     * @param time  The time class to be used in the new schedule.
     * @return      A newly generated schedule.
     */
    public static Schedule generate(IClock time) {
        Schedule schedule = new Schedule(time);

        Random rng = new Random();

        int bound = GENERATOR_MAX_POOL_SIZE - GENERATOR_MIN_POOL_SIZE;
        int queueSize;

        Medication meds[] = Medication.values();

        Queue[] pools = new Queue[] {
            schedule.morningPool, schedule.lunchPool, schedule.eveningPool
        };

        for (Queue pool : pools) {
            queueSize = GENERATOR_MIN_POOL_SIZE + rng.nextInt(bound);
            for (int i = 0; i < queueSize; i++) {
                Medication med = Medication.MISSING;
                while (med == Medication.MISSING) {
                    med = meds[rng.nextInt(meds.length)];
                }
                pool.add(med);
            }
        }

        schedule.validateQueue(false);
        return schedule;
    }

    /**
     * Merging two queues together
     * @author Agnes Petäjävaara
     *
     * @param q1    The activeQueue with the not not taken medication from the previous period
     * @param q2    The medication for the current period
     * @return      One merged queue where the second queue comes after the first
     */
    public Queue<Medication> mergeQueues(Queue<Medication> q1, Queue<Medication> q2) {
        Queue<Medication> resultQueue;
        resultQueue = new LinkedList<>(q1);
        resultQueue.addAll(q2);
        return resultQueue;
    }
}