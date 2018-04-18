package se.medituner.app;
import se.medituner.app.SystemClock;

import java.text.SimpleDateFormat;

public class SchedulePopup {

        public static boolean isItPopupTime(IClock time) {
            SimpleDateFormat timeFormatted = new SimpleDateFormat("HH:mm");

            if(timeFormatted.format(time.now().getTime()).equals("06:06")) {
                return true;
            }else{
                return false;
            }

        }
}
