package se.medituner.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/*
 *  @author Agnes Petäjävaara
 *  A restful Java client
 *  Returns user specific treatment plan from database
 */
public class getRequest {
    public static String getData(){
        String access_token = "pf73ImOQj2Hw0pSu6n3CdnB6p1bpC15ivO9IN-kEqtgLe6pDs6wUrmf0n0A2_EuqR4R47JSiwfa_3YUt2uFWoOFjh7AE0_mFY-lIiGd13VlEs0uRxsxC-6Ydo9r96lkuHdX-D2dRmHZ69GaFGnwfDr_NrTDWaaLhY2otrcgqdjW_9ZYRgiNM0nMjIvl7_zkZdeeSmtI86R2NccD5LtgzV9OANRSOJZOq3KYowhVYVOil_B7_x4fZEimp_iEsCmGmeQINVeMhKT67RknQXs2RcQRvOOduRJT2VNQoGjsdboo_4shXptNbI744h6JoeJS5FbzdPi-lQv1NdPY6BcX76RLUeCnSgg7WNdR4cLw9Te5WrKLmWwWl6wAGAcYgbrPAEqbW1hKHBPHXBIT45A6Vfvh-06_01K52IormiaLt9hzCF5AK4Gj8IGcYxhBuLjfMlIXboLi9Gaf36qcSzN-QVg";
        String API_tPlan = "/api/ver1/treatmentplan/";
        String plan_ID = "C08DC877-ED5C-473F-9C34-AF2D05BEB7A1";

        try {
            URL url = new URL("https://asthmatunerprod.azurewebsites.net" +
                    API_tPlan + plan_ID);
            // setting up connection to URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();    //Opening connection
            connection.setRequestMethod("GET");                                         //Specifying a GET request
            connection.setRequestProperty("Accept", "application/json");                 //Accept data in JSON
            connection.setRequestProperty("Authorization", "Bearer " + access_token);  //FINAL LINE

            //Print out error info if status !OK
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("\nERROR CODE: " + connection.getResponseCode() +
                        "\nERROR MESSAGE: " + connection.getResponseMessage());
            }

            //Build up and return input data stream/treatment plan
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String data;
            while ((data = in.readLine()) != null) {
                sb.append(data);
            }
            connection.disconnect();
            String treatmentPlan = sb.toString();
            // treatmentPlan: {"id":"c08dc877-ed5c-473f-9c34-af2d05beb7a1","patient":"c1954920-1bc6-4c01-be6d-aa3059e21aee","createdBy":{"id":"0d593b73-334b-4ade-a5fb-9f78454cd30c","name":"Peter Sommer","title":"CTO","clinic":{"id":1,"name":"MediTuner Kliniken"}},"timestamp":"2018-04-16T12:59:35.167Z","expired":false,"isUpdated":false,"plans":[{"level":"controlled","instruction":"Glöm inte allergimediciner!","treatments":[{"drugId":"10f9c2c7-1096-4a3b-ae31-4f3a3745d5fa","drugName":"Novopulmon Novolizer","drugImage":"NovopulmonNovolizer.png","drugStrength":"200 microg","morningDose":1,"lunchDose":null,"eveningDose":1,"whenNecessaryDose":null,"whenNecessaryMaxDose":null},{"drugId":"06361544-a01e-4272-88c3-1b10e67aff58","drugName":"Ventilastin Novolizer","drugImage":"VentastinNovolizer.png","drugStrength":"100 microg","morningDose":null,"lunchDose":null,"eveningDose":null,"whenNecessaryDose":2,"whenNecessaryMaxDose":12}]},{"level":"partially-controlled","instruction":"Glöm inte allergimediciner!","treatments":[{"drugId":"06361544-a01e-4272-88c3-1b10e67aff58","drugName":"Ventilastin Novolizer","drugImage":"VentastinNovolizer.png","drugStrength":"100 microg","morningDose":null,"lunchDose":null,"eveningDose":null,"whenNecessaryDose":2,"whenNecessaryMaxDose":12},{"drugId":"5b5a2c9d-63f1-44a0-af6a-4b52afdf6c18","drugName":"Formatris Novolizer","drugImage":"FormatrisNovolizer.png","drugStrength":"12 microg","morningDose":1,"lunchDose":null,"eveningDose":1,"whenNecessaryDose":null,"whenNecessaryMaxDose":null},{"drugId":"10f9c2c7-1096-4a3b-ae31-4f3a3745d5fa","drugName":"Novopulmon Novolizer","drugImage":"NovopulmonNovolizer.png","drugStrength":"200 microg","morningDose":1,"lunchDose":null,"eveningDose":1,"whenNecessaryDose":null,"whenNecessaryMaxDose":null}]},{"level":"not-controlled","instruction":"Glöm inte allergimediciner!","treatments":[{"drugId":"10f9c2c7-1096-4a3b-ae31-4f3a3745d5fa","drugName":"Novopulmon Novolizer","drugImage":"NovopulmonNovolizer.png","drugStrength":"200 microg","morningDose":2,"lunchDose":null,"eveningDose":2,"whenNecessaryDose":null,"whenNecessaryMaxDose":null},{"drugId":"06361544-a01e-4272-88c3-1b10e67aff58","drugName":"Ventilastin Novolizer","drugImage":"VentastinNovolizer.png","drugStrength":"100 microg","morningDose":null,"lunchDose":null,"eveningDose":null,"whenNecessaryDose":2,"whenNecessaryMaxDose":12},{"drugId":"5b5a2c9d-63f1-44a0-af6a-4b52afdf6c18","drugName":"Formatris Novolizer","drugImage":"FormatrisNovolizer.png","drugStrength":"12 microg","morningDose":1,"lunchDose":null,"eveningDose":1,"whenNecessaryDose":null,"whenNecessaryMaxDose":null}]}],"comment":""}
            return treatmentPlan;

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch (ProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
