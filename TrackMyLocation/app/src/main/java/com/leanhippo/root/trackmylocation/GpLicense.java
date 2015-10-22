package com.leanhippo.root.trackmylocation;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;


public class GpLicense extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gp_license);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0XFF428f89));

        String gpLicense = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);

        TextView tv = (TextView) findViewById(R.id.gpLicenseTextView);
        tv.setMovementMethod(new ScrollingMovementMethod());
        if(gpLicense != null){
            tv.setText(gpLicense);
        }
        else{
            tv.setText("Google Play Services is not instaled in this device!");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gp_license, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       



        return super.onOptionsItemSelected(item);
    }
}
