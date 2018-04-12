package se.medituner.app;

import android.app.Activity;
import android.content.SharedPreferences;

//to store and retrieve the medication
public class MedicationPreference {
    SharedPreferences prefs;

    public MedicationPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }
}
