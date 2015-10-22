package com.leanhippo.root.trackmylocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.leanhippo.root.trackmylocation.data.HttpManager;
import com.leanhippo.root.trackmylocation.data.RequestParameterPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class GetNameActivity extends ActionBarActivity {



    private final String PREFS_NAME = "MyPrefsFile";
    private final String NAME_STRING = "name";
    private static final String SHORTENED_NAME_STRING = "shortenedName";
    private static final String USER_ID_STRING = "userId";
    private static final int SHORTENED_NAME_LENGTH = 5;

    public static SharedPreferences settings;
    String NAME = null;
    TextView tv;

    private AlertDialog.Builder internetErrorAlertDialogBuilder;
    private AlertDialog internetErrorAlert;

    private AlertDialog.Builder locationErrorAlertDialogBuilder;
    private AlertDialog locationErrorAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(PREFS_NAME, 0);
        String name = getNameString();
        if(name != null){
            gotoMapsActivity();
        }


        setContentView(R.layout.activity_get_name);

        tv = (TextView) findViewById(R.id.registerStatustextView);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_get_name, menu);
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

    public void nameRegistration(View view) {
        hideSoftKeyboard(this.getCurrentFocus());
        EditText editText = (EditText) findViewById(R.id.nameEditText);
        if(isOnline()){
            String name = String.valueOf(editText.getText());
            if(isnameValid(name)) {
                NAME = name;
                editText.setText("");
                tv.setText("Please wait for a moment...");

                registerNameInServer(name);
            }
            else {
                tv.setText("Invalid entry. Only alphabets are allowed");
            }
        }
        else{
            tv.setText("You are offline. Please connect to the internet");
        }

    }

    private void putNameString(String name){

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(NAME_STRING, name);
        editor.commit();
    }

    private void putShortenedNameString(String shortenedName){

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SHORTENED_NAME_STRING, shortenedName);
        editor.commit();
    }

    private void putUserId(int id){

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(USER_ID_STRING, id);
        editor.commit();
    }


    private String getNameString() {
        return settings.getString(NAME_STRING, null);
    }

    private void gotoMapsActivity(){
        displayLog("passing control to MapsActivity");
        Intent intent = new Intent(this, com.leanhippo.root.trackmylocation.MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private String shortenName(String name) {

        // convert name string to character array
        char[] charArray = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < charArray.length && i < SHORTENED_NAME_LENGTH; i++){
            sb.append(charArray[i]);
        }
        return sb.toString();
    }


    public void registerNameInServer(String name){
        String uri = "http://trackmylocation.co/create_user_id.php";
        RequestParameterPackage p = new RequestParameterPackage();
        p.setMethod("POST");
       // p.setMethod("GET");
        p.setUri(uri);
        p.setParam("name", name);

        connectionToServer connection = new connectionToServer();
        //connection.execute(p);
        connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
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

            parseJSON(s);
            super.onPostExecute(s);
        }



        private void parseJSON(String output){

            if(output !=null)
            {
                displayLog("Output:  " + output);
                try {
                    //displayLog("Json array stage 1");

                    JSONArray jsonArray = new JSONArray(output);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    int userId = jsonObject.getInt("userId");
                    if(userId > 0){
                        putNameString(NAME);
                        putShortenedNameString(shortenName(NAME));
                        putUserId(userId);
                        gotoMapsActivity();
                    }
                    else{
                        tv.setText("Something went wrong. Please try again.");
                    }

                } catch (JSONException e) {
                    tv.setText("Something went wrong. Please try again.");
                    displayLog(" JSON exception" + e.toString());
                    e.printStackTrace();
                }
            }
        }

    }


    private void displayLog(String msg){
        Log.d("MapsActivity", "getNameActivity:   " + msg);
    }


    public void hideSoftKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private boolean isnameValid( String name )
    {
        return name.matches("[a-zA-Z]+(([/s])*([a-zA-Z])*(.)*)*");
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





}