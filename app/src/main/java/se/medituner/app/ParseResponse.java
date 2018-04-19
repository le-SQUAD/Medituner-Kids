package se.medituner.app;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.lang.String;
import java.util.LinkedList;

public class ParseResponse {

    public static final String PLANS = "plans";
    public static final String TREATMENTS = "treatments";
    public static final String DRUG_ID = "drugID";
    public static final String DRUG_NAME = "drugNAME";
    public static final String MORNING_DOSE = "morningDose";
    public static final String LUNCH_DOSE = "lunchDose";
    public static final String EVENING_DOSE = "eveningDose";

    String url = new String("http://now.httpbin.org");
    private LinkedList<JSONObject> meds = new LinkedList<JSONObject>();
    /**
     * Parsing the response from getRequest class to get
     * treatments
     *
     * @author Vendela Vlk
     */
    public LinkedList<JSONObject> parsingData() {

        String jsonStr = getRequest.getData(url);
            try {
                JSONObject treatmentPlan = new JSONObject(jsonStr);
                JSONArray plans = new JSONArray(treatmentPlan.getString(PLANS));

                // Parsing each treatment in each plan
                for(int i=0; i < plans.length(); i++) {
                    JSONObject planObject = plans.getJSONObject(i);
                    JSONArray treatment = new JSONArray(planObject.getString(TREATMENTS));

                    for (int j = 0; j < treatment.length(); j++) {
                        JSONObject medication = treatment.getJSONObject(i);
                        // Each treatment object is put into the LinkedList
                        meds.add(medication);
                    }
                }
            } catch(JSONException e) {
                System.out.print("JSON parsing error: " + e.getMessage());
            }
               return meds;
    }

    }


