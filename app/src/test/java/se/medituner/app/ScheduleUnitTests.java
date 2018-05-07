package se.medituner.app;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.Callable;

public class ScheduleUnitTests {

    private Calendar calendar = Calendar.getInstance();
    private TestClock clock = new TestClock(calendar);
    private Schedule schedule;
    private Streak streak;

    private class TestClock implements IClock {

        public Calendar simulatedNow;

        public TestClock(Calendar calendar) {
            this.simulatedNow = calendar;
        }

        @Override
        public Date now() {
            return simulatedNow.getTime();
        }
    }

    @Before
    public void setUpSchedule() {
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, 8);
        schedule = new Schedule(clock);
        streak = new Streak();
        schedule.connectStreak(streak);

        schedule.addMedToMorningPool(Medication.MISSING);
        schedule.addMedToLunchPool(Medication.AEROBEC);
        schedule.addMedToEveningPool(Medication.ULTIBROBREEZEHALER);
    }

    @Test
    public void scheduleGetPeriodsAreCorrect() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Calendar result = Calendar.getInstance();

        // Before morning
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_MORNING - 1);
        result.setTime(Schedule.getBeginningOfLastPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_LUNCH, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE) - 1, result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfCurrentPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_EVENING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE) - 1, result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfNextPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_MORNING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));

        // After morning, before lunch
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_LUNCH - 1);
        result.setTime(Schedule.getBeginningOfLastPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_EVENING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE) - 1, result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfCurrentPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_MORNING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfNextPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_LUNCH, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));

        // After lunch, before evening
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING - 1);
        result.setTime(Schedule.getBeginningOfLastPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_MORNING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfCurrentPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_LUNCH, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfNextPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_EVENING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));

        // After evening
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING + 1);
        result.setTime(Schedule.getBeginningOfLastPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_LUNCH, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfCurrentPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_EVENING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE), result.get(Calendar.DATE));
        result.setTime(Schedule.getBeginningOfNextPeriod(clock));
        assertEquals(Schedule.PERIOD_BEGINNING_MORNING, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(clock.simulatedNow.get(Calendar.DATE) + 1, result.get(Calendar.DATE));
    }

    @Test
    public void scheduleGetsCorrectQueue() {
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_MORNING + 1);

        schedule.validateQueue(false);
        Queue<Medication> testQueue = schedule.getActiveQueue();
        assertEquals(Medication.MISSING, testQueue.remove());

        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_LUNCH + 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();
        assertEquals(Medication.AEROBEC, testQueue.remove());

        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING + 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();
        assertEquals(Medication.ULTIBROBREEZEHALER, testQueue.remove());

        clock.simulatedNow.add(Calendar.DATE, 1);
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_MORNING + 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();
        assertEquals(Medication.MISSING, testQueue.remove());
    }

    @Test
    public void scheduleCorrectlySkipsPeriods() {
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_MORNING + 1);

        schedule.validateQueue(false);

        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING + 1);
        schedule.validateQueue(false);
        Queue<Medication> testQueue = schedule.getActiveQueue();

        assertEquals(3, testQueue.size());
        assertEquals(Medication.MISSING, testQueue.remove());
        assertEquals(Medication.AEROBEC, testQueue.remove());
        assertEquals(Medication.ULTIBROBREEZEHALER, testQueue.remove());
    }

    @Test
    public void scheduleHandlesDaySkipping() {
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_LUNCH + 1);

        schedule.validateQueue(false);
        Queue<Medication> testQueue = schedule.getActiveQueue();

        assertEquals(2, testQueue.size());
        assertEquals(Medication.MISSING, testQueue.remove());
        assertEquals(Medication.AEROBEC, testQueue.remove());
        schedule.validateQueue(false);

        testQueue = schedule.getActiveQueue();
        assertEquals(0, testQueue.size());

        clock.simulatedNow.add(Calendar.DATE, 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();

        assertEquals(2, testQueue.size());
        assertEquals(Medication.MISSING, testQueue.remove());
        assertEquals(Medication.AEROBEC, testQueue.remove());
    }

    @Test
    public void scheduleUpdatesStreakAccordingly() {
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_MORNING + 1);
        schedule.validateQueue(false);

        // Initial streak is 0
        assertEquals(0, streak.getValue());

        schedule.getActiveQueue().remove();
        schedule.validateQueue(true);

        assertEquals(1, streak.getValue());

        // Skipping to evening
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING + 1);
        schedule.validateQueue(true);
        assertEquals(1, streak.getValue());

        schedule.validateQueue(true);
        assertEquals(1, streak.getValue());

        schedule.validateQueue(false);

        // Skip to next lunch.
        clock.simulatedNow.add(Calendar.DATE, 1);
        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_LUNCH + 1);

        schedule.validateQueue(true);
        assertEquals(0, streak.getValue());

        Queue<Medication> testQueue = schedule.getActiveQueue();
        assertEquals(2, testQueue.size());
        assertEquals(Medication.MISSING, testQueue.remove());
        assertEquals(Medication.AEROBEC, testQueue.remove());
        schedule.validateQueue(true);
        assertEquals(1, streak.getValue());
    }
}
