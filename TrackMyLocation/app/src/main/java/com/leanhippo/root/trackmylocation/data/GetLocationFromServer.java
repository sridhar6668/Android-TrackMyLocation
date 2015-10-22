package com.leanhippo.root.trackmylocation.data;

import android.os.AsyncTask;

import com.leanhippo.root.trackmylocation.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by speriasami on 7/21/2015.
 */
public class GetLocationFromServer {

    private class connectionToServer extends AsyncTask<RequestParameterPackage, String, String> {

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

    public void getLocation(String passCode){
        String uri = "http://trackmylocation.co/get_location.php";
        RequestParameterPackage p = new RequestParameterPackage();
        //p.setMethod("POST");
        p.setMethod("GET");
        p.setUri(uri);
        p.setParam("passCode", passCode);

        connectionToServer connection = new connectionToServer();
        //connection.execute(p);
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
    }

    private void parseJSON(String output){

        if(output !=null)
        {
            //displayLog("Output:  " + output);
            try {
                //displayLog("Json array stage 1");

                JSONArray jsonArray = new JSONArray(output);

                for(int i = 0 ; i < jsonArray.length();i++ ){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                  //  displayLog("Inside json array");
                    String code = jsonObject.getString("passCode");
                    for(ButtonObjectForReceive buttonObject : MapsActivity.buttonList){
                    //    displayLog("updating info for: "+ code);
                            if(code.equals(buttonObject.getCode())){
                            //    displayLog("setting location for"+ buttonObject.getButton().getText());
                                buttonObject.setPreviousLatitude(buttonObject.getLatitude());
                                buttonObject.setPreviousLongitude(buttonObject.getLongitude());
                                buttonObject.setLatitude(jsonObject.getDouble("latitude"));
                                buttonObject.setLongitude(jsonObject.getDouble("longitude"));
                                buttonObject.setDuration(jsonObject.getInt("duration"));
                                buttonObject.setUnixStartTime(jsonObject.getInt("unixStartTime"));
                                buttonObject.setStatus(jsonObject.getInt("status"));
                                buttonObject.setName(jsonObject.getString("name"));

                                // This is for the marker title
                                int tempStopFlag = buttonObject.getStopFlag();
                                buttonObject.setStopFlag(jsonObject.getInt("stopFlag"));
                                if(tempStopFlag != buttonObject.getStopFlag()){
                                    buttonObject.setIdleToActiveFlag(true);
                                }

                                displayLog("Setting new marker ot: " + String.valueOf(buttonObject.getLatitude()) + "  ,  " + String.valueOf(buttonObject.getLongitude()));
                            }
                    }
                }
            } catch (JSONException e) {
                displayLog(" JSON exception" + e.toString());
                e.printStackTrace();
            }
        }
    }
    private static void displayLog(String msg){
           // Log.d("MapsActivity", "GetLocationFromServer:    " + msg);

    }
}
