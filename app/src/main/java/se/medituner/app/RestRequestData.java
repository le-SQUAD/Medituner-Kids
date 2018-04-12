package se.medituner.app;

//To read the API's response into a StringBuffer:
import java.io.BufferedReader;
import java.io.InputStreamReader;
//To make the remote request:
import java.net.HttpURLConnection;
import java.net.URL;
//to convert the response to a JSONObject:
import  org.json.JSONObject;

import android.content.Context;


public class RestRequestData {
    private static final String GET_API_VER1_TREATMENT_PLAN_ID = "http://asthmatuner-dev-ayond.azurewebsites.net/api/ver1/treatmentplan/{id}";

    public static JSONObject getJSON(Context context, String medication){
        try{
            URL url = new URL(String.format(GET_API_VER1_TREATMENT_PLAN_ID, medication));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("header-key", context.getString(R.string.open_get_medication_id));

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            StringBuffer json = new StringBuffer(1024);
            String temporary = "";

            //read in the data from the mediTuner API:
            while((temporary = br.readLine())!= null){
                json.append(temporary).append("\n");
            }
            br.close();

            JSONObject data = new JSONObject(json.toString());

            //404 if ID doesn't match any med:
            if(data.getInt("status")!= 200){
                return null;
            }

            return data;
        }
        catch(Exception e){
            return null;
        }
    }
}
