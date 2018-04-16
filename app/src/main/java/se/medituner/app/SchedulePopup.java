package se.medituner.app;
import se.medituner.app.SystemClock;

import java.util.Calendar;

//MIN HH:mm
//public static final LocalTime MIN;

public class SchedulePopup{
    private IClock time;

        public static boolean isItPopupTime(IClock time) {

            if(time.now().getTime().equals("16:10:00")){
                return true;
            }else if(time.now().getTime().equals("16:08:00")){
                return true;
            }else{
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

}
