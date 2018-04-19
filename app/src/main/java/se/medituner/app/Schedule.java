package se.medituner.app;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

//MIN HH:mm
//public static final LocalTime MIN;

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

    public static boolean isItPopupTime(IClock time) {

        if (time.now().getTime().equals("08:46:00")){
            return true;
        } else if(time.now().getTime().equals("16:35:00")){
            return true;
        } else{
            return false;
        }

      /*
        switch(time){
            case 1:
                time.MIN.equals(09:00);
                questionPopup.showPopupWindow(currentScreen);
                break;
            case 2:
                time.MIN.equals(10:00);
                questionPopup.showPopupWindow(currentScreen);
                break;
        }
        */
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
     * Checks if the current queue is valid, and updates accordingly
     */
    public void validateQueue() {

        if(queueCreationTime.before(getBeginningOfCurrentPeriod(time))){
            if(activeQueue.isEmpty()) {
                //TODO: Check if any periods skipped
                //TODO: Reset streak if YES, increase streak if NO
                updateQueue();
            } else {
                //TODO: Reset streak
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

    /**
     *
     */
    public void resetQueue() {

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

        schedule.validateQueue();
        return schedule;
    }
}