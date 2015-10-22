package com.leanhippo.root.trackmylocation;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.location.Location;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.Binder;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.IBinder;

        import android.provider.Settings;
        import android.support.v4.app.NotificationCompat;
        import android.text.TextUtils;
        import android.util.Log;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.leanhippo.root.trackmylocation.data.UpdateLocationInServer;

        import java.util.Timer;
        import java.util.TimerTask;


public class UpdateCurrentLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static final String BROADCAST_ACTION = "Broadcast location";
    private static final int TRACKER_COMPLETE = 0;
    private static final int TRACKER_RUNNING = 1;
    private static final int ERROR_FLAG_POSITIVE = 1;
    private static final int ERROR_FLAG_NEGATIVE = 0;

    private static final int STOP_FLAG_POSITIVE = 1;
    private static final int STOP_FLAG_NEGATIVE = 0;

    private static final int INTENT_FOR_DEFAULT = 0 ;
    private static final int INTENT_FOR_INTERNET_ERROR = 1;
    private static final int INTENT_FOR_LOCATION_ERROR = 2 ;

    private static final long LOCATION_UPDATE_REQUEST_INTERVAL = 2000;  // IN MILLISECONDS
    private IBinder mBinder = new MyBinder();
    private GoogleApiClient mGoogleApiClient;

    LocationRequest mLocationRequest;
    boolean timeFlag = true;
    long count = 0;

    private Location previousLocation = null;
    private float DISPLAY_DISTANCE = 0;

    private String android_id = null;

    Intent outputIntent;
    private final String PREFS_NAME = "MyPrefsFile";
    private final String TRACKING_END_TIME_STRING = "trackingEndTime";
    private final String TRACKING_START_TIME_STRING = "trackingStartTime";
    private final String TRACK_DURATION_STRING = "trackingDuration";
    private final String TEMP_TRACK_DURATION_STRING = "tempTrackingDuration";
    private final String NAME_STRING = "name";
    private static final String SHORTENED_NAME_STRING = "shortenedName";
    private static final String USER_ID_STRING = "userId";

    private final String SEND_YOUR_LOCATION_UPDATE = "sendYourLocationUpdate";
    private final String SEARCH_FRIENDS_LOCATION = "searchFriendLocation";


    public static SharedPreferences settings;

    private TimerTask remainingTimetimerTask;
    private Timer remainingTimeTimer;
    final Handler remainingTimeHandler = new Handler();
    private long START_TIME = 0;
    private long DURATION = 0;

    private Location currentLocation = null;
    @Override
    public void onCreate() {
        super.onCreate();
        displayLog("Service in onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        displayLog("Service in onstartCommand");
        settings = getSharedPreferences(PREFS_NAME, 0);
        outputIntent = new Intent(BROADCAST_ACTION);

       // START_TIME = Long.getLong(intent.getStringExtra(TRACKING_START_TIME_STRING));
       // DURATION = Long.getLong(intent.getStringExtra(TRACK_DURATION_STRING));
        /*
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            START_TIME = bundle.getLong(TRACKING_START_TIME_STRING);
            DURATION = bundle.getLong(TRACK_DURATION_STRING);
        }
        */
        buildGoogleApiClient();

        cancelAllNotifications();
        startRemainingTimeTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRemainingTimeTimerTask();
        removeLocationUpdates();
        createBroadcast(0, 0, STOP_FLAG_POSITIVE, ERROR_FLAG_NEGATIVE);
        cancelNotification(TRACKER_RUNNING);
        if(currentLocation != null){
            UpdateLocationInServer updateLocationInServer = new UpdateLocationInServer();
            updateLocationInServer.updateLocation(generatePassCode(), currentLocation, getTempTrackDuration(), getNameString(), getuserId(), 1);
        }
        displayLog("Service in onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

        @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    protected synchronized void buildGoogleApiClient() {

        displayLog("Service build google api client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }
    private void cancelNotification(int id){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
    private void cancelAllNotifications(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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
    public void onConnected(Bundle bundle) {
        displayLog("Service onconnected");
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 6668, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setInterval(getTimeToUpdateYourLocation() * 1000);
        mLocationRequest.setFastestInterval(getTimeToUpdateYourLocation() * 1000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.

        displayLog("Inside location change listener: " + String.valueOf(getUnixTime()) + " Interval: " + String.valueOf(getTimeToUpdateYourLocation()));
        currentLocation = location;
        count++;
        displayLog( String.valueOf(count));

            displayLog("Current unix time: " + String.valueOf((System.currentTimeMillis()/1000)));
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            int errorFlag = ERROR_FLAG_POSITIVE;

        if(isOnline()){


            if(previousLocation != null){
                if (location.distanceTo(previousLocation) < DISPLAY_DISTANCE){
                    //Do nothing
                }
                else{
                    //Updating the location change in server

                    UpdateLocationInServer updateLocationInServer = new UpdateLocationInServer();
                    updateLocationInServer.updateLocation(generatePassCode(), location, getTempTrackDuration(), getNameString(), getuserId(), 0);
                }
            }else

            {
                //Updating the location change in server
                UpdateLocationInServer updateLocationInServer = new UpdateLocationInServer();
                updateLocationInServer.updateLocation(generatePassCode(), location, getTempTrackDuration(), getNameString(), getuserId(), 0);
            }

            errorFlag = ERROR_FLAG_NEGATIVE;

        }

            // Add markers in the map
            //addBreadcrumbMarker(lat, lng);
            //addMarker("Testing", lat, lng, 0);
            //gotoLocation(lat, lng, mMap.getCameraPosition().zoom);
            previousLocation = location;
            if(getTrackingEndTime() < MapsActivity.getUnixTime()){
            // you need to stop the service now.
                createBroadcast(location.getLatitude(), location.getLongitude(), STOP_FLAG_POSITIVE, ERROR_FLAG_NEGATIVE);
                stopSelf();
            }
            else{
                createBroadcast(location.getLatitude(), location.getLongitude(), STOP_FLAG_NEGATIVE, errorFlag);
            }
            //createNotification();

        return;
    }

    private void createBroadcast(double latitude, double longitude, int stopFlag, int errorFlag){

        outputIntent.setAction(MapsActivity.mBroadcastStringAction);
        outputIntent.putExtra(getString(R.string.latitude), String.valueOf(latitude));
        outputIntent.putExtra(getString(R.string.longitude), String.valueOf(longitude));

        //check whether we past the end time if so then end service


        outputIntent.putExtra(getString(R.string.stopFlag), String.valueOf(stopFlag));
        outputIntent.putExtra(getString(R.string.errorFlag), String.valueOf(errorFlag));
        sendBroadcast(outputIntent);


    }

    public void startRemainingTimeTimer() {

        remainingTimeTimer = new Timer();
        initializeRemainingTimeTimerTask();
        remainingTimeTimer.schedule(remainingTimetimerTask, 0, 1000);

    }

    public void stopRemainingTimeTimerTask() {
        //stop the remainingTimeTimer, if it's not already null
        if (remainingTimeTimer != null) {
            remainingTimeTimer.cancel();
            remainingTimeTimer = null;
        }
    }

    public void initializeRemainingTimeTimerTask() {

        remainingTimetimerTask = new TimerTask() {
            public void run() {

                //use a remainingTimeHandler to run a toast that shows the current timestamp
                remainingTimeHandler.post(new Runnable() {
                    public void run() {
                        int intentFlag = 0;
                        String content = null;
                        String title = null;

                        String minutesFirstString = "";
                        String secondsFirstString = "";

                        long minutes = 0;
                        long seconds = 0;
                        long remainingTime = getTrackingEndTime() - MapsActivity.getUnixTime();

                        if (remainingTime >= 0) {
                            minutes = remainingTime / 60;
                            if (minutes < 10) {
                                minutesFirstString += "0";
                            }

                            seconds = remainingTime % 60;
                            if (seconds < 10) {
                                secondsFirstString += "0";
                            }
                            if (!isLocationEnabled(getApplicationContext())) {
                                title = getString(R.string.locationErrorTitle);
                                content = getString(R.string.locationErrorMessage);
                                intentFlag = INTENT_FOR_LOCATION_ERROR;
                            } else if (isOnline()) {
                                title = "Tracker is active.";
                                content = "Remaining Time:\n" +
                                        minutesFirstString + String.valueOf(minutes) +
                                        ":" +
                                        secondsFirstString + String.valueOf(seconds);
                            } else {
                                title = getString(R.string.internetErrorTitle);
                                content = getString(R.string.internetErrorMessage);
                                intentFlag = INTENT_FOR_INTERNET_ERROR;
                            }
                            createNotification(intentFlag, title, content, TRACKER_RUNNING);
                        } else {
                            title = "Tracking complete.";
                            content = "Thank you for using TrackMyLocation";
                            createNotification(intentFlag, title, content, TRACKER_COMPLETE);
                            stopSelf();
                        }


                    }

                });
            }
        };
    }

    public static long getUnixTime(){
        return (System.currentTimeMillis()/1000);
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
    private long getTempTrackDuration(){
        return  settings.getLong(TEMP_TRACK_DURATION_STRING, 0);
    }


    private String getNameString() {
        return settings.getString(NAME_STRING, null);
    }
    private String getShortenedNameString() {
        return settings.getString(SHORTENED_NAME_STRING, null);
    }

    public int getTimeToSearchFriendsLocation(){
        return settings.getInt(SEARCH_FRIENDS_LOCATION, 3);
    }

    public int getTimeToUpdateYourLocation(){
        return settings.getInt(SEND_YOUR_LOCATION_UPDATE, 2);
    }
    private int getuserId() {
        return settings.getInt(USER_ID_STRING, 0);
    }


    private void createNotification(int intentFlag, String title, String content, int id) {


            //displayLog("Creating Notification");
            // Prepare intent which is triggered if the
            // notification is selected
            Intent intent;
            switch(intentFlag){
                case INTENT_FOR_INTERNET_ERROR:
               //     intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    intent = new Intent();
                    intent.setComponent(new ComponentName(
                            "com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));

                    break;
                case INTENT_FOR_LOCATION_ERROR:
                    intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    break;
                default:
                    intent = new Intent(this, GetNameActivity.class);
            }



            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Build notification
            // Actions are just fake
            NotificationCompat.Builder noti = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(content)
                    //.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon))
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pIntent)
                    //.setDefaults(Notification.DEFAULT_SOUND)
                    //.setDefaults(Notification.DEFAULT_VIBRATE)
                    ;

            // hide the notification after its selected

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(id, noti.build());

    }
    private String findTimeDifference(){
        long difference = getTrackingEndTime() - MapsActivity.getUnixTime();
        if(difference >= 0){
            return String.valueOf(difference) + "secs";
        }
        else{
            return 0 + "secs";
        }
    }
    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }

        return false;
    }

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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void removeLocationUpdates(){
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch(Exception e){
            displayLog(e.getMessage());
        }
    }
    public void displayLog(String msg){
        //Log.d("MapsActivity", "Background service:    " + msg);
    }

    public class MyBinder extends Binder {
        UpdateCurrentLocationService getService() {
            return UpdateCurrentLocationService.this;
        }
    }
}