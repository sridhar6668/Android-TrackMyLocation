package com.leanhippo.root.trackmylocation.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by root on 7/8/15.
 */
public class HttpManager {

public static String makeHttpRequest(RequestParameterPackage p) throws IOException {
    String uri = p.getUri();
    BufferedReader reader = null;
    if(p.getMethod().equals("GET")){
        uri += "?" + p.getEncodedParams();
    }
    try{
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(p.getMethod());
        //conn.setRequestProperty("Connection", "close");
        displayLog("Connecting to URL:" + uri + p.getEncodedParams());
        if(p.getMethod().equals("POST")){
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(p.getEncodedParams());
            writer.flush();
        }
        displayLog("Starting to receive");
        StringBuilder sb = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        displayLog("Stage 1");
        while((line = reader.readLine()) != null){
            displayLog(line);
            sb.append(line + "\n");
        }
        displayLog("Message ends*****************");
        return sb.toString();
    }catch(UnknownHostException e){
        displayLog("Trying to run" + e.toString());
        return null;
    }
    catch(EOFException e){
        displayLog("Trying to run + EOF exception" + e.toString());
        return null;
    }
    catch(IOException e){
        displayLog("Trying to run + IO exception" + e.toString());
        return null;
    }
    catch(Exception e){
        displayLog("Exception in makeHttpRequest" + e.toString());
        return null;
    }finally {

        if(reader != null){
            try {
                reader.close();
            }catch (IOException e){
                e.printStackTrace();
                displayLog("Exception in makeHttpRequest reader close");
                return null;
            }

        }
    }

}

    private static void displayLog(String msg){
    //  Log.d("MapsActivity", "HTTPManager.java:   " + msg);
    }

}
