package se.medituner.app;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * A clock that provides now() Calendar.
 */
public interface IClock extends Serializable {
    Date now();
}
