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
        assertEquals(Medication.MISSING, testQueue.element());

        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_LUNCH + 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();
        assertEquals(Medication.AEROBEC, testQueue.element());

        clock.simulatedNow.set(Calendar.HOUR_OF_DAY, Schedule.PERIOD_BEGINNING_EVENING + 1);
        schedule.validateQueue(false);
        testQueue = schedule.getActiveQueue();
        assertEquals(Medication.ULTIBROBREEZEHALER, testQueue.element());
    }
}
