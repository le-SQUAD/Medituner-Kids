package se.medituner.app;

import java.util.Calendar;

public class SystemClock implements IClock {

    @Override
    public Calendar now() {
        return Calendar.getInstance();
    }
}
