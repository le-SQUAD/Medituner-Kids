package se.medituner.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
/*
 *  @author agnes
 *  A restful Java client, skeleton code from  https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/
 */
public class getRequest {

    public static void main(String[] args){
        String access_token = "pf73ImOQj2Hw0pSu6n3CdnB6p1bpC15ivO9IN-kEqtgLe6pDs6wUrmf0n0A2_EuqR4R47JSiwfa_3YUt2uFWoOFjh7AE0_mFY-lIiGd13VlEs0uRxsxC-6Ydo9r96lkuHdX-D2dRmHZ69GaFGnwfDr_NrTDWaaLhY2otrcgqdjW_9ZYRgiNM0nMjIvl7_zkZdeeSmtI86R2NccD5LtgzV9OANRSOJZOq3KYowhVYVOil_B7_x4fZEimp_iEsCmGmeQINVeMhKT67RknQXs2RcQRvOOduRJT2VNQoGjsdboo_4shXptNbI744h6JoeJS5FbzdPi-lQv1NdPY6BcX76RLUeCnSgg7WNdR4cLw9Te5WrKLmWwWl6wAGAcYgbrPAEqbW1hKHBPHXBIT45A6Vfvh-06_01K52IormiaLt9hzCF5AK4Gj8IGcYxhBuLjfMlIXboLi9Gaf36qcSzN-QVg";
        String patientId = "c1954920-1bc6-4c01-be6d-aa3059e21aee";
        String userId = "98578477-2dff-49a6-9f47-78fef33363f9";
        String API_treatmentplan = "/api/ver1/treatmentplan/";                          //The API for the treatmentplan, should be followed by an ID

        try{   // URL example : http://example.com/resource?x=y&bearer_token=vF9dft4qmT
            URL url = new URL( "https://asthmatunerprod.azurewebsites.net" +
                    API_treatmentplan + patientId +"?bearer_token=" + access_token);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();    //Opening connection
            connection.setRequestMethod("GET");                                         //Specifying a GET request
            connection.setRequestProperty("Accept","application/json");                 //Accept data in JSON

            connection.
            //Print out error info if Status != 200 OK
            if(connection.getResponseCode() != 200){
                throw new RuntimeException("\nERROR CODE: " + connection.getResponseCode() +
                        "\nERROR MESSAGE: " + connection.getResponseMessage());
            }

            //Print out the stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }

            connection.disconnect();

         //Exceptions:
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
