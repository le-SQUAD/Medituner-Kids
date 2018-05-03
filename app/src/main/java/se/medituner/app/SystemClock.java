package se.medituner.app;

import java.util.Calendar;
import java.util.Date;

public class SystemClock implements IClock {

    @Override
    public Date now() {
        return Calendar.getInstance().getTime();
    }
}
