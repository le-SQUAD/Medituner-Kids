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
     * @authors Greg?, Ola?
     *          Agnes Petäjävaara(latest update), added not taken medication to the current pool
     */
    private void updateQueue() {
        Date periodBeginning = getBeginningOfCurrentPeriod(time);
        queueCreationTime = time.now().getTime();
        Calendar cal = time.now();
        cal.setTime(periodBeginning);
        switch (cal.get(Calendar.HOUR_OF_DAY)) {

            //Checks if the all medication the previous day was taken, if so increase the streak and
            //create a new morning pool of medication
            case 5:
               /*
                if(!activeQueue.isEmpty() || getBeginningOfLastPeriod(time).after(queueCreationTime)){
                    streak.reset();
                }else{
                    streak.increment();
                }
                */
                activeQueue = new LinkedList<>(morningPool);
                break;

            case 11:
                if(activeQueue.isEmpty()){
                    activeQueue = new LinkedList<>(lunchPool);
                    break;
                }else{
                    activeQueue = new LinkedList<>(mergeQueues(activeQueue,lunchPool));
                    break;
                }
            default:
                if(activeQueue.isEmpty()){
                    activeQueue = new LinkedList<>(eveningPool);
                    break;
                }else{
                    activeQueue = new LinkedList<>(mergeQueues(activeQueue,eveningPool));
                    break;
                }


           /* case 11:
                activeQueue = new LinkedList<>(lunchPool);
                break;

            default:
                activeQueue = new LinkedList<>(eveningPool);
                break;
            */
        }
    }

    /**
     * Checks if the current queue is valid, and updates accordingly.
     *
     * @param updateStreak Should the streak be updated? (incremented or reset)
     * @author Aleksandra Soltan, Grigory Glukhov
     */
    public void validateQueue(boolean updateStreak) {
        if (queueCreationTime.before(getBeginningOfCurrentPeriod(time))) {
            System.out.println("Updating queue");
            System.out.print(streak);
            System.out.print(" ");
            System.out.println(updateStreak);
            if (updateStreak && streak != null) {
                System.out.println("Checking for reset");
                if (!activeQueue.isEmpty() || getBeginningOfLastPeriod(time).after(queueCreationTime)) {
                    streak.reset();
                }
            }
            updateQueue();
            streakUpdated = false;
        } else {
            if (activeQueue.isEmpty() && !streakUpdated) {
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
     * @param time IClock interface for now() method.
     * @return Date corresponding to the beginning of the current period (back in time).
     * @author Aleksandra Soltan, Grigory Glukhov
     */
    public static Date getBeginningOfCurrentPeriod(IClock time) {
        Calendar now = time.now();
        Calendar comparison = time.now();
        comparison.set(Calendar.MINUTE, 0);
        comparison.set(Calendar.SECOND, 0);
        comparison.set(Calendar.MILLISECOND, 0);

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
        comparison.set(Calendar.MILLISECOND, 0);

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

    /**
     * Reset the active queue to the current period pool.
     *
     * WARNING!
     * This is not functional behavior for the class and the method should only be used for testing!
     */
    public void resetQueue() {
        Calendar calendar = time.now();
        calendar.add(Calendar.DATE, -1);
        queueCreationTime = calendar.getTime();
        validateQueue(false);
    }

    /**
     * Generate a new schedule.
     * The new schedule generated will have between GENERATOR_MIN_POOL and GENERATOR_MAX_POOL non missing items in each pool
     *
     * @param time The time class to be used in the new schedule.
     * @return A newly generated schedule.
     */
    public static Schedule generate(IClock time) {
        Schedule schedule = new Schedule(time);

        Random rng = new Random();

        int bound = GENERATOR_MAX_POOL_SIZE - GENERATOR_MIN_POOL_SIZE;
        int queueSize = GENERATOR_MIN_POOL_SIZE + rng.nextInt(bound);

        Medication meds[] = Medication.values();

        for (int i = 0; i < queueSize; i++) {
            Medication med = Medication.MISSING;
            while (med == Medication.MISSING) {
                med = meds[rng.nextInt(meds.length)];
            }
            schedule.addMedToMorningPool(med);
        }

        queueSize = GENERATOR_MIN_POOL_SIZE + rng.nextInt(bound);
        for (int i = 0; i < queueSize; i++) {
            Medication med = Medication.MISSING;
            while (med == Medication.MISSING)
                med = meds[rng.nextInt(meds.length)];
            schedule.addMedToLunchPool(med);
        }

        queueSize = GENERATOR_MIN_POOL_SIZE + rng.nextInt(bound);
        for (int i = 0; i < queueSize; i++) {
            Medication med = Medication.MISSING;
            while (med == Medication.MISSING)
                med = meds[rng.nextInt(meds.length)];
            schedule.addMedToEveningPool(med);
        }

       schedule.validateQueue(false);
        return schedule;
    }

    /**
     * Merging two queues together
     * @author Agnes Petäjävaara
     *
     * @param    q1 = the activeQueue with the not not taken medication from the previous period
     *           q2 = the medication for the current period
     * @return one merged queue where the second queue comes after the first
     */
    private static Queue mergeQueues(Queue q1, Queue q2) {
        Queue<Object> resultQueue = null;
        if (q1.isEmpty()) return q2;
        else if (q2.isEmpty()) return q1;
        else {
            while (!q1.isEmpty()){
                resultQueue.add(q1.poll());
            }
            while(!q2.isEmpty()){
                resultQueue.add(q2.poll());
            }
            return resultQueue;
        }
    }
}