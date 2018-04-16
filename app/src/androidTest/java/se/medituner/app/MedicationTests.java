package se.medituner.app;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MedicationTests {

    @Test
    public void each_medication_is_present() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();


        for (Medication med : Medication.values()) {
            if (Medication.getImageId(appContext.getResources(), appContext.getPackageName(), med) == 0)
                fail("Medication resource not found for " + med.toString());
            assertNotNull(Medication.getName(appContext.getResources(),
                    appContext.getPackageName(), med));

        }
    }
}