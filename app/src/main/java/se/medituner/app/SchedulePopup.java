package se.medituner.app;
import se.medituner.app.SystemClock;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

//MIN HH:mm
//public static final LocalTime MIN;

public class SchedulePopup{
    //private IClock time;
        final Queue<Medication> morning;
        final Queue<Medication> lunch;
        final Queue<Medication> evening;
        Queue<Medication> activeQueue;

        public SchedulePopup(Queue<Medication> morningQueue, Queue<Medication> lunchQueue, Queue<Medication> eveningQueue){
            morning = morningQueue;
            lunch = lunchQueue;
            evening = eveningQueue;
            activeQueue = new LinkedList<Medication>();
        }

        public static boolean isItPopupTime(IClock time) {

            if(time.now().getTime().equals("08:46:00")){
                return true;
            }else if(time.now().getTime().equals("16:35:00")){
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

        public Queue getCurrentQueue(){
              return activeQueue;
        }

        public void updateMorningQueue(){
            activeQueue = new LinkedList<>(morning);
        }

        public void updateLunchQueue(){
            activeQueue = new LinkedList<>(lunch);
        }

        public void updateEveningQueue(){
            activeQueue = new LinkedList<>(evening);
        }
}
