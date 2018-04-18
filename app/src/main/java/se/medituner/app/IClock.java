package se.medituner.app;

import java.io.Serializable;
import java.util.Calendar;

/**
 * A clock that provides now() Calendar.
 */
public interface IClock extends Serializable {
    Calendar now();
}
