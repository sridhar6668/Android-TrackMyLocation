package com.leanhippo.root.trackmylocation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.leanhippo.root.trackmylocation.data.HttpManager;
import com.leanhippo.root.trackmylocation.data.RequestParameterPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EnterCode extends ActionBarActivity {

    private static final int ALPHABETS_ONLY = 1;
    private static final int NUMBERS_ONLY = 2;
    private static final int ALPHA_NUMERIC = 3;
    private static final String ACTIVE_CODES_SET = "activeCodesSet";


    private final String PREFS_NAME = "MyPrefsFile";
    private final String TRACKING_END_TIME = "trackingEndTime";
    private final String TRACKING_START_TIME = "trackingStartTime";
    private final String TRACK_DURATION = "trackingDuration";
    public static SharedPreferences settings;
    private static String VALIDATED_CODE = null;
    TextView tv;

    private static boolean REDIRECT_FLAG = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0XFF428f89));

        tv = (TextView) findViewById(R.id.statustextView);
        EditText editText = (EditText) findViewById(R.id.enterCodeTextField);
        settings = getSharedPreferences(PREFS_NAME, 0);


        Uri data = getIntent().getData();
        if(data != null){
            REDIRECT_FLAG = true;
            editText.setVisibility(View.GONE);
            Button button = (Button) findViewById(R.id.addCodeButton);
            button.setVisibility(View.GONE);

            processCodeFromLink(data);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enter_code, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* ---------------------------------- Custom Functions start --------------------------------------------- */

    private void processCodeFromLink(Uri data){

        String scheme = data.getScheme();
        String host = data.getHost();
        List<String> params = data.getPathSegments();

        String code = params.get(0);
        if(isCodeValid(code)) {
            VALIDATED_CODE = code;
            validateCodeInServer(code);

        }
        else{
            tv.setText("Invalid code.");
        }

    }

    public void addCode(View view) {
        displayLog("addCode");
        EditText editText = (EditText) findViewById(R.id.enterCodeTextField);
        String code = String.valueOf(editText.getText());
        if(isCodeValid(code)) {

            editText.setText("");


            VALIDATED_CODE = code;
            validateCodeInServer(code);

            tv.setText("Validating your code. Please wait...");


            //Add the new code into the array and commit
            displayLog("Exiting addCode");
        }
        else{
            tv.setText("Invalid code.");
        }

       
    }

    private boolean isCodeValid( String code )
    {
        return code.matches("(([a-zA-Z0-9])*(-)*)+");
    }

    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }
        displayLog("isOnline return false");
        return false;
    }
    public void validateCodeInServer(String passCode){

        if(isOnline()) {
            displayLog("Validating code");
            String uri = "http://trackmylocation.co/get_location.php";
            RequestParameterPackage p = new RequestParameterPackage();
            p.setMethod("POST");
            //p.setMethod("GET");
            p.setUri(uri);
            p.setParam("passCode", passCode);

            connectionToServer connection = new connectionToServer();
            //connection.execute(p);
            connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
        }
        else{
            tv.setText("You are offline. Please turn on the internet and try again.");
        }
    }


    public  void appendCodeString(){

        // Get the previously stored active codes strings
        String codeString = getCodeString(ACTIVE_CODES_SET);
        displayLog("CodeString: " + codeString);
        if(codeString != null) {
            displayLog("Inside if");
            ArrayList<String> codeStringArrayList = new ArrayList<String>(Arrays.asList(codeString.split("_")));
            if(!codeStringArrayList.contains(VALIDATED_CODE)){
                codeStringArrayList.add(VALIDATED_CODE);
                //String codeStringArray[] = (String[]) codeStringArrayList.toArray();
                displayLog("Inside string builder");
                StringBuilder sb = new StringBuilder();
                for(String s: codeStringArrayList){
                    displayLog("Inside for");
                    if(sb.length() > 0){
                        sb.append("_");
                    }
                    sb.append(s);
                }

                putCodeString(sb.toString(), ACTIVE_CODES_SET);
            }else{
                tv.setText("Code already exsists!");
            }

        }
        else
        {
            displayLog("Inside else");
            putCodeString(VALIDATED_CODE, ACTIVE_CODES_SET);
        }

        if(REDIRECT_FLAG == true){
            redirect();
        }

    }

    private void redirect(){

        displayToast("Code successfully added.");
        Intent intent = new Intent(this, com.leanhippo.root.trackmylocation.GetNameActivity.class);
        startActivity(intent);
        finish();

    }

    private void putCodeString(String codeString, String codeId) {

        displayLog("putCodeString");

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACTIVE_CODES_SET, codeString);
        editor.commit();
        displayLog("Set: " + codeString);
        tv.setText("Your code has been successfully added.");
    }

    private  String getCodeString(String codeId) {
        displayLog("getCodeString");
       return settings.getString(ACTIVE_CODES_SET, null);
       
    }

    private void removeCode(String code){
        displayLog("Inside removeCode");

        if(code == null){
            return;

        }
        displayLog("Trying to delete: " + code);

        displayToast("Trying to delete:" + code);

        // Get the previously stored active codes strings
        String codeString = getCodeString(ACTIVE_CODES_SET);

        if(codeString != null) {
            displayLog("Inside if");
            ArrayList<String> codeStringArrayList = new ArrayList<String>(Arrays.asList(codeString.split("_")));
            if(codeStringArrayList.contains(code)){
                codeStringArrayList.remove(code);
                //String codeStringArray[] = (String[]) codeStringArrayList.toArray();
                displayLog("Inside string builder");
                StringBuilder sb = new StringBuilder();
                for(String s: codeStringArrayList){
                    displayLog("Inside for");
                    if(sb.length() > 0){
                        sb.append("_");
                    }
                    sb.append(s);
                }

                putCodeString(sb.toString(), ACTIVE_CODES_SET);
            }

        }

    }









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


            super.onPostExecute(s);
            parseJSON(s);

        }

    }



    private void parseJSON(String output){

        if(output !=null)
        {

            try {
                //displayLog("Json array stage 1");

                JSONArray jsonArray = new JSONArray(output);

                for(int i = 0 ; i < jsonArray.length();i++ ){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //  displayLog("Inside json array");
                    String code = jsonObject.getString("passCode");
                    long duration = jsonObject.getInt("duration");
                    long unixStartTime = jsonObject.getInt("unixStartTime");
                    int status = jsonObject.getInt("status");

                    if((status == 1)) {
                        if(((unixStartTime + duration - getUnixTime()) > 0)) {
                            String toBeRemovedCode =  checkForMultipleEntriesFromSameUser(jsonObject);
                            removeCode(toBeRemovedCode);
                            appendCodeString();

                        }
                        else{
                            tv.setText("The code you have entered has expired.");
                        }
                    }else{
                        tv.setText("Invalid code.");
                    }


                }
            } catch (JSONException e) {
                tv.setText("Something went wrong. Please try again...");
                displayLog(" JSON exception" + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                displayLog(" IO exception while writing to allCodeJSON file" + e.toString());
                e.printStackTrace();
            }
        }
        else{
            tv.setText("Something went wrong. Please try again...");
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String checkForMultipleEntriesFromSameUser(JSONObject jsonObject) throws IOException, JSONException {
        File dir = getFilesDir();
        String path = dir.getAbsolutePath();
        displayToast("Path: "+ path);


        displayLog("Inside checkForMultipleEntriesFromSameUser");
        String toBeRemovedCode = null;
        String fileName = "allCodeJSON.txt";
        File file = new File(dir, fileName);
        JSONArray jsonArray = new JSONArray();
        if(file.exists()) {
            displayLog("Inside fileinput stream");
            // Get contents of the file
            FileInputStream fis = openFileInput(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            StringBuffer sb = new StringBuffer();
            while (bis.available() != 0) {
                char c = (char) bis.read();
                sb.append(c);
            }
            bis.close();
            fis.close();

            displayLog("File contents:" + sb.toString());
            jsonArray = new JSONArray(sb.toString());

            // check for the multiple user id and delete it

            boolean flag = false;
            int index = -1;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempJsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getLong("userId") == tempJsonObject.getLong("userId")) {
                    displayLog("Multiple entries found");
                    index = i;
                    flag = true;
                    toBeRemovedCode = tempJsonObject.getString("passCode");
                }
            }
            if (flag == true) {
                displayLog("Multiple entries removed");
                jsonArray.remove(index);
            }

        }
        else{
            displayLog("Creating new file");
            file.createNewFile();
            displayLog("New file created");
        }
        // Append new json object to json array and write it to the file
        displayLog("Adding new json object to file");
        jsonArray.put(jsonObject);
        String jsonString = jsonArray.toString();
        FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
        fos.write(jsonString.getBytes());

        displayLog("File Content: " + jsonString);
        fos.close();

        return toBeRemovedCode;
    }


    private long getUnixTime(){
        return (System.currentTimeMillis()/1000);
    }


    private void displayToast(String s){

        // Toast.makeText(this, " " + s, Toast.LENGTH_LONG).show();
    }

    private void displayLog(String msg){
        Log.d("MapsActivity", "EnterCode.java:   " + msg);
    }

}

