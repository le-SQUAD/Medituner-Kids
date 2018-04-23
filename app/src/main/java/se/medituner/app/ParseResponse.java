package se.medituner.app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.lang.String;
import java.util.ArrayList;

public class ParseResponse extends AsyncTask<Void, Void, Void> {

    public static final String PLANS = "plans";
    public static final String TREATMENTS = "treatments";
    public static final String DRUG_ID = "drugID";
    public static final String DRUG_NAME = "drugName";
    public static final String MORNING_DOSE = "morningDose";
    public static final String LUNCH_DOSE = "lunchDose";
    public static final String EVENING_DOSE = "eveningDose";

    /**
     * Parse the response from getRequest class to get
     * specific treatment information.
     *
     * @author Vendela Vlk
     */
    //public static ArrayList<JSONObject> parsingData() {
    /*@Override
    protected void onPreExecute() {
    super.onPreExecute();
    }*/

    @Override
    protected Void doInBackground(Void... args0) {
        StringBuilder medName = new StringBuilder();
        ArrayList<JSONObject> meds = new ArrayList<>();

        String data = getRequest.getData();
        Log.d("TAG", data);
        try {
            // Make treatmentplan string into JSON object
            JSONObject treatmentPlan = new JSONObject(data);
            JSONArray plans = treatmentPlan.getJSONArray(PLANS);
            // Parsing each plan object
            for(int i = 0; i < plans.length(); i++) {
                JSONObject planObject = plans.getJSONObject(i);
                Log.d("TAG", planObject.getString("instruction"));
                JSONArray treatment = planObject.getJSONArray(TREATMENTS);
                // Parsing each treatment in each plan
                for (int j = 0; j < treatment.length(); j++) {
                    JSONObject medication = treatment.getJSONObject(j);
                    //medName.append(medication.getString(DRUG_NAME) + "\n");
                    medName.append(medication + "\n");
                    // Each treatment object is put into the ArrayList
                    meds.add(medication);
                }
            }
        } catch(JSONException e) {
            System.out.print("JSON parsing error: " + e.getMessage());
        }
        Log.d("TAG", medName.toString());
        return null;
    }
    /*
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }*/
}


