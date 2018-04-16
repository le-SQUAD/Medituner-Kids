package se.medituner.app;

import android.content.res.Resources;

public enum Medication {

    AEROBEC,
    AEROBECAUTOHALER;

    public static int getImageId(Resources res, String packageName, Medication medication) {
        return res.getIdentifier(medication.name().toLowerCase(), "drawable", packageName);
    }

    public static String getName(Resources res, String packageName, Medication medication) {
        return res.getString(res.getIdentifier(medication.name().toLowerCase(), "string", packageName));
    }
}
