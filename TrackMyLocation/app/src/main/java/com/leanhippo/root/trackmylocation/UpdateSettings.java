package com.leanhippo.root.trackmylocation;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class UpdateSettings extends ActionBarActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private final String PREFS_NAME = "MyPrefsFile";
    public static SharedPreferences settings;

    private final String SEND_YOUR_LOCATION_UPDATE = "sendYourLocationUpdate";
    private final String SEARCH_FRIENDS_LOCATION = "searchFriendLocation";

    Integer[] timeArray = {2,3,5,10,15,30,60,120,300,600};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_settings);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0XFF428f89));

        settings = getSharedPreferences(PREFS_NAME, 0);
        setUpDurationSpinner(R.id.shareLocationUpdateSettingsSpinner, getTimeToUpdateYourLocation());
        setUpDurationSpinner(R.id.friendsLocationUpdateSettingsSpinner, getTimeToSearchFriendsLocation());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.menu_update_settings, menu);
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


    public int getTimeToSearchFriendsLocation(){
        return settings.getInt(SEARCH_FRIENDS_LOCATION, 3);
    }

    public int getTimeToUpdateYourLocation(){
        return settings.getInt(SEND_YOUR_LOCATION_UPDATE, 2);
    }

    private void setUpDurationSpinner(int id, int duration){

        Spinner spinner = (Spinner) findViewById(id);
        List<String> list = new ArrayList<String>();
        list.add("Every 2 seconds");
        list.add("Every 3 seconds");
        list.add("Every 5 seconds");
        list.add("Every 10 seconds");
        list.add("Every 15 seconds");
        list.add("Every 30 seconds");
        list.add("Every 1 minute");
        list.add("Every 2 minutes");
        list.add("Every 5 minutes");
        list.add("Every 10 minutes");



        int selectedItemIndex = Arrays.asList(timeArray).indexOf(duration);


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        displayToast("Selected item index: "+String.valueOf(selectedItemIndex));
        spinner.setSelection(selectedItemIndex);
    }

    public void updateTimeSettings(View view) {

        Spinner yourLocationUpdateSpinner = (Spinner) findViewById(R.id.shareLocationUpdateSettingsSpinner);
        Spinner friendsLocationUpdateSpinner = (Spinner) findViewById(R.id.friendsLocationUpdateSettingsSpinner);

        int yourLocationUpdateTime = timeArray[yourLocationUpdateSpinner.getSelectedItemPosition()];
        int friendsLocationUpdateTime = timeArray[friendsLocationUpdateSpinner.getSelectedItemPosition()];

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SEND_YOUR_LOCATION_UPDATE, yourLocationUpdateTime);
        editor.putInt(SEARCH_FRIENDS_LOCATION, friendsLocationUpdateTime);
        editor.commit();
        displayToast(String.valueOf(friendsLocationUpdateTime));
        displayToastPublished("Settings updated.");

    }

    private void displayToast(String s){
      //    Toast.makeText(this, "Message: " + s, Toast.LENGTH_LONG).show();
    }

    private void displayToastPublished(String s){

        Toast toast = Toast.makeText(this," "+ s, Toast.LENGTH_LONG);
        toast.show();
        //Toast.makeText(this, " "+ s , Toast.LENGTH_LONG).show();
    }

}
