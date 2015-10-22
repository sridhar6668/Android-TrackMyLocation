package com.leanhippo.root.trackmylocation.data;

import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by root on 7/8/15.
 */
public class UpdateLocationInServer {

    private class connectionToServer extends AsyncTask<RequestParameterPackage, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(RequestParameterPackage... params) {
            HttpManager httpManager = new HttpManager();
            String output = null;
            try {
                output = httpManager.makeHttpRequest(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String s) {

            parseJSON(s);
            super.onPostExecute(s);

        }

    }

    public void updateLocation(String passCode, Location location, long duration, String name, int userId, int stopFlag){
        String uri = "http://trackmylocation.co/update_location.php";
        RequestParameterPackage p = new RequestParameterPackage();
        //p.setMethod("POST");
        p.setMethod("GET");
        p.setUri(uri);
        p.setParam("passCode", passCode);
        p.setParam("latitude", String.valueOf(location.getLatitude()));
        p.setParam("longitude", String.valueOf(location.getLongitude()));
        p.setParam("duration", String.valueOf(duration));
        p.setParam("name", name);
        p.setParam("userId", String.valueOf(userId));
        p.setParam("stopFlag", String.valueOf(stopFlag));
        connectionToServer connection = new connectionToServer();
        //connection.execute(p);
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
    }

    private void parseJSON(String output){

        if(output !=null)
        {
            displayLog("Output:  " + output);
            try {
                //JSONArray jsonArray = new JSONArray(output);
                JSONObject jsonObject = new JSONObject(output);
                //displayLog("Output JSON" + jsonObject.getInt("result"));
            } catch (JSONException e) {
                //displayLog("Inside parse JSON exception");
                e.printStackTrace();
            }
        }
    }
    private static void displayLog(String msg){
       // Log.d("MapsActivity", "UpdateLocationInServer:    " + msg);
    }

}
