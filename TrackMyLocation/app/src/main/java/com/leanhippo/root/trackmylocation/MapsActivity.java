package com.leanhippo.root.trackmylocation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.PolylineOptions;
import com.leanhippo.root.trackmylocation.data.ButtonObjectForReceive;
import com.leanhippo.root.trackmylocation.data.GetLocationFromServer;
import com.leanhippo.root.trackmylocation.data.UserInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.leanhippo.root.trackmylocation.UpdateCurrentLocationService.MyBinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends  ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    public static final String mBroadcastStringAction = "com.truiton.broadcast.string";
    public static final String mBroadcastIntegerAction = "com.truiton.broadcast.integer";
    public static final String mBroadcastArrayListAction = "com.truiton.broadcast.arraylist";
    public static double CURRENT_LATITUDE = 0;
    public static double CURRENT_LONGITUDE = 0;

    public static final String CODE_STRING = "code";
    private static final int ERROR_FLAG_POSITIVE = 1;
    private static final int ERROR_FLAG_NEGATIVE = 0;

    private static final int STOP_FLAG_POSITIVE = 1;
    private static final int STOP_FLAG_NEGATIVE = 0;
    private static final int BLACK = 0;
    private static final long LOCATION_UPDATE_REQUEST_INTERVAL = 2000;
    private static long FOCUS_COUNT = 1;
    private static long FOCUS_CURRENT_LOCATION_COUNT = 1; // Initially equal to FOCUS_COUNT. So current location will be in focus initially

    private final String PREFS_NAME = "MyPrefsFile";
    private final String TRACKING_END_TIME_STRING = "trackingEndTime";
    private final String TRACKING_START_TIME_STRING = "trackingStartTime";
    private final String TRACK_DURATION_STRING = "trackingDuration";
    private final String TEMP_TRACK_DURATION_STRING = "tempTrackingDuration";
    private final String SEND_YOUR_LOCATION_UPDATE = "sendYourLocationUpdate";
    private final String SEARCH_FRIENDS_LOCATION = "searchFriendLocation";
    private final String NAME_STRING = "name";
    private static final String USER_ID_STRING = "userId";

    public static final long IDLE_THERSHOLD = 3;

    private static final String ACTIVE_CODES_SET = "activeCodesSet";

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    public static SharedPreferences settings;
    //FragmentActivity
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double shareLat = 11.449678;
    private double shareLng = 77.679842;
    private String shareAddressString;
    private static final float defaultZoom = 13;
    Marker marker;
    Marker currentLocationMarker;
    Marker pathTraceMarker;
    GoogleApiClient mGoogleApiClient;

    private Location previousLocation = null;
    private double previousLat = 0;
    private double previousLng = 0;
    private float displayDistance = 30;

    long count;
    private LocationRequest mLocationRequest;
    private boolean TRACKING_FLAG = false;
    private UserInfo userInfo;
    private String android_id = null;
    private TimerTask remainingTimeTimerTask;
    private Timer remainingTimeTimer;
    final Handler remainingTimeHandler = new Handler();

    public static ArrayList<ButtonObjectForReceive> buttonList = null;

    private Integer[] colorArray = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.MAGENTA};
    UpdateCurrentLocationService mBoundService;
    boolean mServiceBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinder myBinder = (MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };

    IntentFilter intentFilter = new IntentFilter(mBroadcastStringAction);

//    private NumberPicker np;

    private AlertDialog.Builder internetErrorAlertDialogBuilder;
    private AlertDialog internetErrorAlert;

    private AlertDialog.Builder locationErrorAlertDialogBuilder;
    private AlertDialog locationErrorAlert;

    ButtonObjectForReceive youButtonObject;

    private int initialiseButtonCount;

/* ----------------------------------- Declaration ends ----------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (serviceOK()) {
            setContentView(R.layout.activity_maps);
            Log.d(getString(R.string.maps_activity), "onCreate");

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(0XFF428f89));

            setUpMapIfNeeded();
           // Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();

            //getSupportActionBar().setIcon(R.drawable.icon);
                //mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                settings = getSharedPreferences(PREFS_NAME, 0);
                initialiseSharedPreferenceChangeListener();

                // By default the app wont focus on the current location. We have to do it manually.
                //  So get last known location and and move focus to it.

                buildGoogleApiClient();


                android_id = getUniqueDeviceId();
                displayLog("Before number picker");
                //setUpNumberPicker();
                setUpDurationSpinner();

                trackingPresent();
                displayLog("After Tracking present");
                registerReceiver(mReceiver, intentFilter);
                setupInternetErrorAlertDialogBox();
                verifyInternetEnabled();
                setupLocationErrorAlertDialogBox();
                verifyLocationEnabled();

                startRemainingTimeTimer();

                initialiseButtonList();

                initialiseCameraChangeListener();

//            Toast.makeText(this, "Loading complete" , Toast.LENGTH_LONG).show();



        }
    }


    @Override
    protected void onResume() {
        super.onResume();
       // setUpMapIfNeeded();
    }

    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(getString(R.string.maps_activity), "onCreateOptionsMenu");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent intent;
        switch(id) {
            case R.id.gpLicense:
                 intent = new Intent(this, GpLicense.class);
                startActivity(intent);
                return true;
            case R.id.faq:
                intent = new Intent(this, FAQActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings:
                intent = new Intent(this, UpdateSettings.class);
                startActivity(intent);
                return true;
         /*
            case R.id.enterCode:
                intent = new Intent(this, EnterCode.class);
                startActivity(intent);
                return true;
            case R.id.deleteCode:
                intent = new Intent(this, DeleteCode.class);
                startActivity(intent);
                return true;
                */
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Log.d(getString(R.string.maps_activity), "Inside Destroy");

            unregisterReceiver(mReceiver);
        stopRemainingTimeTimerTask();
        super.onDestroy();



    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(getString(R.string.maps_activity), "Inside configuration changed");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
              //  setUpMap();

            }
        }
    }

    /**
     * This is wh/ere we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public boolean serviceOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 100);
            dialog.show();
        } else {
            Toast.makeText(this, "Something wrong!", Toast.LENGTH_LONG).show();
        }
        return false;
    }


/* ----------------------------------- Custom function starts ----------------------------------- */




    private void initialiseSharedPreferenceChangeListener(){
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(ACTIVE_CODES_SET) ){
                    initialiseButtonList();

                }
                else if(key.equals(SEND_YOUR_LOCATION_UPDATE)){
                    mGoogleApiClient.disconnect();
                    buildGoogleApiClient();
                    startBackgroundServiceSequence();

                }
            }
        };
        settings.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private void setupInternetErrorAlertDialogBox() {
        displayLog("Inside internetErrorAlert dialog box");
        internetErrorAlertDialogBuilder = new AlertDialog.Builder(this);
        internetErrorAlertDialogBuilder.setTitle(getString(R.string.internetErrorTitle))
                .setMessage(getString(R.string.internetErrorMessage));
        internetErrorAlertDialogBuilder.setPositiveButton(getString(R.string.goToSettings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivityForResult(intent, 0);


            }
        });
        internetErrorAlertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        internetErrorAlert = internetErrorAlertDialogBuilder.create();

    }

    private void showInternetErrorAlertDialogBox(){  internetErrorAlert.show();    }

    private void hideInternetErrorAlertDialogBox(){  internetErrorAlert.cancel();    }

    private void verifyInternetEnabled() {
        if(!(isOnline())){
            showInternetErrorAlertDialogBox();
        }
    }



    private void setupLocationErrorAlertDialogBox() {
        displayLog("Inside locationErrorAlert dialog box");
        locationErrorAlertDialogBuilder = new AlertDialog.Builder(this);
        locationErrorAlertDialogBuilder.setTitle(getString(R.string.locationErrorTitle))
                .setMessage(getString(R.string.locationErrorMessage));
        locationErrorAlertDialogBuilder.setPositiveButton(getString(R.string.goToSettings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

            }
        });
        locationErrorAlertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        locationErrorAlert = locationErrorAlertDialogBuilder.create();

    }

    private void showLocationErrorAlertDialogBox(){  locationErrorAlert.show();   }

    private void hideLocationErrorAlertDialogBox(){  locationErrorAlert.cancel();  }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void verifyLocationEnabled() {
        if(!isLocationEnabled(getApplicationContext())){
            showLocationErrorAlertDialogBox();
        }
    }
    /**
     * mapCurrentLocationlistener initialises
     */


    private void setUpDurationSpinner(){

        Spinner spinner = (Spinner) findViewById(R.id.durationSpinner);
        List<String> list = new ArrayList<String>();
        list.add("30 minutes");
        list.add("1 hour");
        list.add("1.5 hours");
        list.add("2 hours");
        list.add("2.5 hours");
        list.add("3 hours");
        list.add("3.5 hours");
        list.add("4 hours");


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(3);
    }


    /**
     *
     */

    private void gotoLocation(double lat, double lng) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 13);
        mMap.moveCamera(update);

    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.animateCamera(update);
    }


    /**
     *
     * @param locality
     * @param lat
     * @param lng
     */

    private void addMarker(String locality, double lat, double lng){
        if(marker != null){
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(new LatLng(lat, lng))
                .draggable(true);

        marker = mMap.addMarker(options);
        if(locality !=null){
            marker.setTitle(locality);
        }
        marker.setSnippet("Latitude: " + lat + "\n" + "Longitude: " + lng);
    }


    @Override
    public void onConnected(Bundle bundle) {


        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(getString(R.string.maps_activity), "inside onConnected");
        double lat;
        double lng;

        if(location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            gotoLocation(lat, lng);
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
/*
    public void gotoCurrentLocation(View view){
        Log.d(getString(R.string.maps_activity), "Inside gotoCurrentLocation");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }
   */

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    public void openShareOptions() {

        Intent shareOptionsActivityIntent = new Intent(this, com.leanhippo.root.trackmylocation.ShareOptionsActivity.class);

        shareOptionsActivityIntent.putExtra(CODE_STRING, generatePassCode());
        startActivity(shareOptionsActivityIntent);
/*
        Intent shareOptionsActivityIntent = new Intent(this, com.leanhippo.root.trackmylocation.ShareWithContacts.class);
        startActivity(shareOptionsActivityIntent);
*/

    }
    public void openShareOptions(View view){
        openShareOptions();
    }

    private String generatePassCode(){
        return getUniqueDeviceId()+"-"+String.valueOf(getTrackingStartTime());
    }

    private String getUniqueDeviceId(){

        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        /*
        TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
        */

    }

    @Override
    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        displayLog("Inside location change listener: " + String.valueOf(getUnixTime()) + " Interval: " + String.valueOf(getTimeToUpdateYourLocation()));



        double lat = location.getLatitude();
        double lng = location.getLongitude();

            // Add markers in the map
        currentLocationMarker = setMarker(currentLocationMarker, lat, lng, R.drawable.star);

        CURRENT_LATITUDE = lat;
        CURRENT_LONGITUDE = lng;
        if(FOCUS_CURRENT_LOCATION_COUNT == FOCUS_COUNT) {
            gotoLocation(lat, lng, mMap.getCameraPosition().zoom);
        }

        return;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void startLocationUpdates(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getTimeToUpdateYourLocation() * 1000);
        mLocationRequest.setFastestInterval(getTimeToUpdateYourLocation() * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void startBackgroundServiceSequence(){
        displayLog("startBackgroundServiceSequence()");
        stopBackgroundService();

        startBackgroundService();
        //bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }



    private void stopBackgroundServiceSequence(){
        /*
        if (mServiceBound) {
            displayLog("Maps activtity - unbinding service");
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        */

        stopBackgroundService();
    }

    private void startBackgroundService(){
        displayLog("startBackgroundService()");
        Intent intent = new Intent(this, UpdateCurrentLocationService.class);

        startService(intent);

    }

    private void stopBackgroundService(){
        displayLog("stopBackgroundService");
        Intent intent = new Intent(this, UpdateCurrentLocationService.class);
        stopService(intent);
    }


    private void confirmationDialogBoxForShareOptions(final long trackDuration) {
        displayLog("Inside Share options alert dialog box");
        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialogBuilder.setTitle(getString(R.string.shareOptionsConfirmationDialogBoxTitle))
                .setMessage(getString(R.string.shareOptionsConfirmationMessage));

        AlertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setTrackingStartTime();
                setTrackingDurationTime(trackDuration);
                startBackgroundServiceSequence();

                setUpStopButton();
                openShareOptions();
            }
        });
        AlertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setTrackingDurationTime(trackDuration + getUnixTime() - getTrackingStartTime());
                startBackgroundServiceSequence();
                setUpStopButton();

            }
        });
        AlertDialogBuilder.create().show();

    }
    public void trackingStartStop(View view){
        try {

            if (TRACKING_FLAG == false) {
                Log.d(getString(R.string.maps_activity), "Tracking started");
                Spinner spinner = (Spinner) findViewById(R.id.durationSpinner);


                //startLocationUpdates();
                /**
                 * Because we are getting back the index not the value.
                 * So if we need 60, we will get 5 , because 5 is the index of 60
                 */
                displayToast(String.valueOf(spinner.getSelectedItemId()));
                long trackDuration = ((spinner.getSelectedItemId() + 1) * 30) * 60;
                if(((getUnixTime() - getTrackingStartTime())/(60 * 60)) < 24){
                    confirmationDialogBoxForShareOptions(trackDuration);
                }
                else{

                    setTrackingStartTime();
                    setTrackingDurationTime(trackDuration);
                    startBackgroundServiceSequence();

                    setUpStopButton();
                    openShareOptions();
                }



                //startBackgroundServiceSequence(view, 10);


            } else {
                Log.d(getString(R.string.maps_activity), "Tracking paused");
                setUpStartButton();
                //stopLocationUpdates();
                stopBackgroundServiceSequence();
                resetTrackDurationTime();


            }
        }
        catch(Exception e){
            Log.d(getString(R.string.maps_activity), e.getMessage());
        }
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



    public static long getUnixTime(){
        return (System.currentTimeMillis()/1000);
    }

    private void setTrackingDurationTime(long duration){
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(TRACK_DURATION_STRING, duration);
        editor.putLong(TEMP_TRACK_DURATION_STRING, duration);
        editor.commit();
      //  displayLog("End time stored in preferences. Start Time:" + String.valueOf(startTime));
        //displayLog(" Duration:" + String.valueOf(duration));
    }

    private void setTrackingStartTime(){
        SharedPreferences.Editor editor = settings.edit();
        long startTime = getUnixTime();
        editor.putLong(TRACKING_START_TIME_STRING, startTime);
        editor.commit();
        //displayLog("End time stored in preferences. Start Time:" + String.valueOf(startTime));
        //displayLog(" Duration:" + String.valueOf(duration));
    }
    private void resetTrackDurationTime(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(TRACK_DURATION_STRING, 0);
        editor.commit();
        //displayLog(" Duration:" + String.valueOf(duration));
    }

    public long getTrackingEndTime(){
        return getTrackingStartTime() + getTrackDuration();

    }
    public long getTrackingStartTime(){
        return settings.getLong(TRACKING_START_TIME_STRING, 0);
    }
    public long getTrackDuration(){

        return settings.getLong(TRACK_DURATION_STRING, 0);
    }

    public int getTimeToSearchFriendsLocation(){
        return settings.getInt(SEARCH_FRIENDS_LOCATION, 3);
    }

    public int getTimeToUpdateYourLocation(){
        return settings.getInt(SEND_YOUR_LOCATION_UPDATE, 2);
    }

    private String getNameString() {
        return settings.getString(NAME_STRING, null);


    }


    private void trackingPresent(){
        //displayLog("Inside trackingPresent");
        if(isTrackingActive()){
            // display stop button
            setUpStopButton();
            startBackgroundServiceSequence();

        }
        else{
            // display start button
            setUpStartButton();
            stopBackgroundServiceSequence();

        }

    }

    private void setUpStopButton() {
        displayLog("Setting up Stop Button");
        LinearLayout startButtonLayout=(LinearLayout)this.findViewById(R.id.startButtonLayout);
        startButtonLayout.setVisibility(LinearLayout.GONE);
        LinearLayout stopButtonLayout=(LinearLayout)this.findViewById(R.id.stopButtonLayout);
        stopButtonLayout.setVisibility(LinearLayout.VISIBLE);
        TRACKING_FLAG = true;

    }
    private void setUpStartButton() {
        displayLog("Setting up Start Button");
        LinearLayout stopButtonLayout=(LinearLayout)this.findViewById(R.id.stopButtonLayout);
        stopButtonLayout.setVisibility(LinearLayout.GONE);
        LinearLayout startButtonLayout=(LinearLayout)this.findViewById(R.id.startButtonLayout);
        startButtonLayout.setVisibility(LinearLayout.VISIBLE);
        TRACKING_FLAG = false;

    }



    private void addMarker(String locality, double lat, double lng, int currentLocationFlag){
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .anchor(0.5f, 0.5f)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_blue));
        currentLocationMarker = mMap.addMarker(options);
        if(locality !=null){
            currentLocationMarker.setTitle(locality);
        }
        currentLocationMarker.setSnippet("Latitude: " + lat + "\n" + "Longitude: " + lng);
    }



    private void setMakerTitle(Marker marker, String title){
        if(marker != null){
            marker.setTitle(title);
        }
    }

    private Marker setMarker(Marker marker, double lat, double lng, String title){
        if(marker != null){
            marker.setPosition(new LatLng(lat, lng));
            if(title != null){
                marker.setTitle(title);
            }

            return marker;
            //marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .anchor(0.5f, 0.5f)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_blue));
        return mMap.addMarker(options);
    }

    private Marker setMarker(Marker marker, double lat, double lng){
        return setMarker(marker, lat, lng, null);

    }

    private Marker setMarker(Marker marker, double lat, double lng, int resourceId){
        if(marker != null){
            marker.setPosition(new LatLng(lat, lng));
            return marker;
            //marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .anchor(0.5f, 0.5f)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(resourceId));
        return mMap.addMarker(options);

    }

    private void removeMarker(Marker marker){
        if(marker != null){
            marker.remove();
        }
    }





    public void startRemainingTimeTimer() {

        remainingTimeTimer = new Timer();
        initializeRemainingTimeTimerTask();
        remainingTimeTimer.schedule(remainingTimeTimerTask, 0, 1000);

    }

    public void stopRemainingTimeTimerTask() {
        //stop the remainingTimeTimer, if it's not already null
        if (remainingTimeTimer != null) {
            remainingTimeTimer.cancel();
            remainingTimeTimer = null;
        }
    }

    public void initializeRemainingTimeTimerTask() {

        remainingTimeTimerTask = new TimerTask() {
            public void run() {

                //use a remainingTimeHandler to run a toast that shows the current timestamp
                remainingTimeHandler.post(new Runnable() {
                    public void run() {


                        if(TRACKING_FLAG == true) {
                            displayRemainingTime();
                        }

                        if(getUnixTime()%getTimeToSearchFriendsLocation() == 0 || ((initialiseButtonCount--) > 0))
                        {

                            displayLog("Calling display log *******************************************************************");
                            displayButtons();
                        }


                    }
                });
            }
        };
    }

    private void displayButtons(){

        boolean ButtonsPresentFlag = false;
        LinearLayout layout = (LinearLayout) findViewById(R.id.activeCodesButtonLayout);
        if(buttonList != null) {

            String codeString = getCodeString(ACTIVE_CODES_SET);
            if (codeString != null) {
                //displayLog("Code String not null");
                GetLocationFromServer getLocationFromServer = new GetLocationFromServer();
                getLocationFromServer.getLocation(codeString);

                for (ButtonObjectForReceive buttonObject : buttonList) {
                    displayLog("iterating button list");
                    buttonObject.getButton().setVisibility(View.GONE);
                    long remainingTime = buttonObject.getUnixStartTime() + buttonObject.getDuration() - getUnixTime();
                    if (buttonObject.getStatus() == 1){

                        if(remainingTime > 0) {

                            displayLog("updating location for" + buttonObject.getButton().getText());
                            layout.setVisibility(View.VISIBLE);
                            ButtonsPresentFlag = true;

                            buttonObject.getButton().setVisibility(View.VISIBLE);

                            String name = buttonObject.getName();
                            buttonObject.getButton().setText(name);

                            double lat = buttonObject.getLatitude();
                            double lng = buttonObject.getLongitude();

                            double previousLat = buttonObject.getPreviousLatitude();
                            double previousLng = buttonObject.getPreviousLongitude();
                          //  displayLog("Setting new marker for " + buttonObject.getButton().getText() + "  at: " + String.valueOf(lat) + "  ,  " + String.valueOf(lng));

                            String title;

                            displayLog("Gonna start showing marker");
                            if(buttonObject.getStopFlag() == 1){

                                displayLog("Inside if");
                                // Change the marker status to inactive to notify the user.
                                if(buttonObject.isIdleToActive()){
                                    buttonObject.setIdleToActiveFlag(false);
                                    if(buttonObject.getMarker() != null){
                                        buttonObject.getMarker().setTitle(buttonObject.getName() + ": Inactive");
                                        buttonObject.getMarker().showInfoWindow();
                                    }
                                }
                                title = buttonObject.getName() + ": Inactive";
                                buttonObject.getButton().setBackgroundResource(R.drawable.user_button_white);
                                buttonObject.getButton().setTextColor(0xFFCCCCCC);
                            }
                            else{
                                displayLog("Inside else");
                                // User is active
                                if(buttonObject.isIdleToActive()){
                                    buttonObject.setIdleToActiveFlag(false);
                                    if(buttonObject.getMarker() != null) {
                                        buttonObject.getMarker().setTitle(buttonObject.getName() + ": Active");
                                        buttonObject.getMarker().showInfoWindow();
                                    }
                                }
                                title = buttonObject.getName() + ": Active";
                                buttonObject.getButton().setBackgroundResource(R.drawable.user_button_white);
                                buttonObject.getButton().setTextColor(0xFF428f89);
                            }

                            buttonObject.setMarker(setMarker(buttonObject.getMarker(), lat, lng, title));
                            if(buttonObject.getFocusCount() == FOCUS_COUNT) {
                                gotoLocation(lat, lng, mMap.getCameraPosition().zoom);
                                if(buttonObject.getStopFlag() != 1) {
                                    buttonObject.getButton().setBackgroundResource(R.drawable.user_button);
                                    buttonObject.getButton().setTextColor(0xFF428f89);
                                }

                            }


                            if(previousLat != 0 ) {
                                drawLine(previousLat, previousLng, lat, lng, 0xff64B5F6, 20);
                                drawLine(previousLat, previousLng, lat, lng, 0xff2196F3, 10);
                            }

                        }
                        else if(((getUnixTime() - buttonObject.getUnixStartTime())/ (3600)) >=24 ){
                            displayToast("Removing expired dead code " + buttonObject.getCode());
                            displayLog("Removing dead code after 24 hours");
                            removeCode(buttonObject.getCode());

                        }
                        else{
                            removeMarker(buttonObject.getMarker());

                        }
                    }
                }
            }
        }
        if(ButtonsPresentFlag == false){
            layout.setVisibility(View.GONE);
        }
    }
    private void putCodeString(String codeString, String codeId) {

        displayLog("putCodeString");

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACTIVE_CODES_SET, codeString);
        editor.commit();
        displayLog("Set: " + codeString);
    }


    private String getCodeString(String codeId) {
        //displayLog("getCodeString");
        return settings.getString(ACTIVE_CODES_SET, null);

    }
    private void removeCode(String code){

        displayLog("Inside removeCode");
        if(code == null){
            return;
        }
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

    private void initialiseButtonList() {

        displayLog("initialiseActiveCodes");
        LinearLayout layout = (LinearLayout)findViewById(R.id.activeCodesButtonLayout);
        layout.setBackgroundColor(Color.GRAY);
        layout.removeAllViews();
        mMap.clear();
        currentLocationMarker = null;
        String codeString = getCodeString(ACTIVE_CODES_SET);
        if(codeString != null) {

            // Create a list of button for active codes
            buttonList = new ArrayList<ButtonObjectForReceive>();
            final String[] codeStringArray = codeString.split("_");
            for(int i = 0; i < codeStringArray.length; i++){
                // We need a list of buttons but buttons has more information to it. So we are using a class that contains button, code, lat and lng.
                Button button = new Button(this);
                final String code = codeStringArray[i];
                final ButtonObjectForReceive buttonObjectForReceive = new ButtonObjectForReceive("User " + String.valueOf(i + 1), code ,button);
                buttonObjectForReceive.setLineColor(colorArray[i % colorArray.length]);
                button.setId(i);
                button.setText("User " + String.valueOf(i + 1));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.weight = 1;
                layoutParams.setMargins(4, 2, 4, 2);
                //layoutParams
                button.setLayoutParams(layoutParams);

              //  button.setBackgroundColor(colorArray[i % colorArray.length]);
                buttonObjectForReceive.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FOCUS_COUNT++;
                        buttonObjectForReceive.setFocusCount(FOCUS_COUNT);
                        buttonObjectForReceive.getMarker().showInfoWindow();
                        gotoLocation(buttonObjectForReceive.getLatitude(), buttonObjectForReceive.getLongitude(), mMap.getCameraPosition().zoom);
                    }
                });

                buttonObjectForReceive.getButton().setLongClickable(true);
                buttonObjectForReceive.getButton().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        confirmationDialogBoxForCodeStringDelete(buttonObjectForReceive.getName(), buttonObjectForReceive.getCode(), buttonObjectForReceive.getMarker());
                       // removeCode(code);
;                        return false;
                    }
                });

                layout.addView(buttonObjectForReceive.getButton());
                buttonList.add(i, buttonObjectForReceive);
            }
            displayToastPublished("Click on the username to see his location.");
            displayToastPublished("Click on the username to see his location.");
        }

        initialiseButtonCount = 10;
    }

    private void confirmationDialogBoxForCodeStringDelete(String name, final String code, final Marker marker) {
        displayLog("Inside internetErrorAlert dialog box");
        AlertDialog.Builder codeStringDeleteAlertDialogBuilder = new AlertDialog.Builder(this);
        codeStringDeleteAlertDialogBuilder.setTitle(getString(R.string.codeDeleteConfirmationTitle))
                .setMessage(name + " - " + code);
        codeStringDeleteAlertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeMarker(marker);
                removeCode(code);
            }
        });
        codeStringDeleteAlertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        codeStringDeleteAlertDialogBuilder.create().show();

    }

    private void initialiseCameraChangeListener(){

        displayLog("Camera location changed");

    }

    private void displayRemainingTime() {
        //displayLog("displayRemainingTime");
        String text = null;
        String minutesFirstString = "";
        String secondsFirstString = "";
        long minutes = 0;
        long seconds = 0;
        TextView tv = (TextView) findViewById(R.id.remainingTimeTextView);
        long remainingTime = getTrackingEndTime() - getUnixTime();
        if (remainingTime >= 0) {
            minutes = remainingTime / 60;
            if (minutes < 10) {
                minutesFirstString += "0";
            }
            seconds = remainingTime % 60;
            if (seconds < 10) {
                secondsFirstString += "0";
            }
            text = "Time Left:\n" +
                    minutesFirstString + String.valueOf(minutes) +
                    ":" +
                    secondsFirstString + String.valueOf(seconds);
        } else {
            text = "Time Left:\n" + "00:00";
            stopBackgroundServiceSequence();
        }
        tv.setText(text);

        /**
         * check whether location service is enabled for evey 10 seconds. If not show an alert dialog.
         */

        if ((seconds % 10) == 0) {
            verifyLocationEnabled();
        }
    }


    private boolean isTrackingActive(){
        //displayLog("Inside isTrackingActive");
        long trackingEndTime = getTrackingEndTime();
        long currentTime = getUnixTime();
        if(trackingEndTime > currentTime ){
            return true;
        }
        return false;
    }

    private void drawLine(double previousLat, double previousLng, double currentLat, double currentLng, int color){

        //displayLog("drawLine");
        drawLine(previousLat, previousLng, currentLat, currentLng, color, 10);
    }

    private void drawLine(double previousLat, double previousLng, double currentLat, double currentLng, int color, float width){

        //displayLog("drawLine");

        PolylineOptions polyLine = new PolylineOptions()
                .add(new LatLng(previousLat, previousLng))
                .add(new LatLng(currentLat, currentLng))
                .color(color)
                .width(width)
                ;

        mMap.addPolyline(polyLine);
    }




    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int stopFlag = Integer.parseInt(intent.getStringExtra(getString(R.string.stopFlag)));
            if(stopFlag == STOP_FLAG_POSITIVE){
                setUpStartButton();
            }
            else{
                setUpStopButton();
            }

            int errorFlag = Integer.parseInt(intent.getStringExtra(getString(R.string.errorFlag)));
            if(errorFlag == ERROR_FLAG_POSITIVE){
                showInternetErrorAlertDialogBox();
            }
            else{
                hideInternetErrorAlertDialogBox();
            }

            double lat = Double.parseDouble(intent.getStringExtra(getString(R.string.latitude)));
            double lng = Double.parseDouble(intent.getStringExtra(getString(R.string.longitude)));

           // displayLog("Broadcast received. errorFlag: " + errorFlag);
            if(lat!=0) { // Just to make sure , the app dont crash.

                if(previousLat != 0){
                     //  drawLine(previousLat, previousLng, lat, lng, 0xff000000);
                }
                previousLat = lat;
                previousLng = lng;
                //gotoLocation(lat, lng, mMap.getCameraPosition().zoom);
            }

        }


    };

    private void displayToast(String s){
      //  Toast.makeText(this, "Message: "+ s , Toast.LENGTH_LONG).show();
    }

    private void displayToastPublished(String s){

        Toast toast = Toast.makeText(this," "+ s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        //Toast.makeText(this, " "+ s , Toast.LENGTH_LONG).show();
    }

    private static void displayLog(String msg){

        Log.d("MapsActivity", msg);
    }

    public void focusCurrentLocation(View view) {

        displayLog("focusCurrentLocation");
        FOCUS_COUNT++;
        FOCUS_CURRENT_LOCATION_COUNT = FOCUS_COUNT;

        if(CURRENT_LATITUDE != 0){
            gotoLocation(CURRENT_LATITUDE, CURRENT_LONGITUDE, mMap.getCameraPosition().zoom);
        }
    }
}
