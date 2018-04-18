package se.medituner.app;
import se.medituner.app.SystemClock;

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
public class Schedule {

    // Dependency injection
    private IClock time;

    // Schedule pools
    private final Queue<Medication> morning;
    private final Queue<Medication> lunch;
    private final Queue<Medication> evening;

    // Schedule active queue.
    private Date queueCreationTime;
    private Queue<Medication> activeQueue;

    public Schedule(Queue<Medication> morningQueue, Queue<Medication> lunchQueue, Queue<Medication> eveningQueue){
        morning = morningQueue;
        lunch = lunchQueue;
        evening = eveningQueue;
        activeQueue = new LinkedList<Medication>();
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
        queueCreationTime = getBeginningOfCurrentPeriod(time);
        Calendar cal = time.now();
        cal.setTime(queueCreationTime);
        switch (cal.get(Calendar.HOUR_OF_DAY)) {
            case 5:
                activeQueue = new LinkedList<>(morning);
                break;

            case 11:
                activeQueue = new LinkedList<>(lunch);
                break;

            default:
                activeQueue = new LinkedList<>(evening);
                break;
        }
    }

    public void validateQueue() {

    }

    /**
     * Get the beginning of the current period (morning, lunch or evening).
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

    public Queue getCurrentQueue(){
          return activeQueue;
    }
}
