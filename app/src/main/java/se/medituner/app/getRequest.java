package se.medituner.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
/*
*   A restful Java client, skeleton code from:
*   https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/
 */
public class getRequest {

    //http://asthmatuner-dev-ayond.azurewebsites.net/api/ver1/treatmentplan/{id}

    public static String getData(String reqUrl){

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(reqUrl);
            //URL url = new URL("http://now.httpbin.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept","text/plain");

            if(connection.getResponseCode() != 200){
                throw new RuntimeException("\t\n" +
                        "Errorcode=8000 , drug not found.\r\n" + connection.getResponseCode());
            }
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(connection.getInputStream()));


            String line;

            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
