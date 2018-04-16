package se.medituner.app;

import java.util.Calendar;

//MIN HH:mm
//public static final LocalTime MIN;

public class SchedulePopup implements IClock{



        Calendar time;

        public static boolean isItPopupTime(Calendar time) {

            if (time.equals('09:00')){
                return true;
            }else if (time.equals('08:00')){
                return true;
            }else{
                return false;
            }
        }
          /*  switch(time){
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
