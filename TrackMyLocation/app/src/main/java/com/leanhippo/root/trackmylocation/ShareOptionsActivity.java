package com.leanhippo.root.trackmylocation;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.leanhippo.root.trackmylocation.data.CustomListViewArrayAdapter;


public class ShareOptionsActivity extends ActionBarActivity {

    String codeString;
    String[] shareListViewString = {"Message", "WhatsApp", "Gmail", "Copy to clipboard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_options);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0XFF428f89));
        codeString = getIntent().getStringExtra(com.leanhippo.root.trackmylocation.MapsActivity.CODE_STRING);
     //   displayCode(codeString);
        setUpListView((ListView) findViewById(R.id.shareListView));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.gpLicense) {
            Intent gpIntent = new Intent(this, GpLicense.class);
            startActivity(gpIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

/*
    private void displayCode(String codeString) {
        EditText editText = (EditText) findViewById(R.id.codeText);
        editText.setText(codeString);
    }
    */



    private void setUpListView(final ListView listView){



        listView.setVisibility(View.VISIBLE);

        String[] shareListViewString = {"Message", "WhatsApp", "Gmail", "Copy to clipboard"};
        CustomListViewArrayAdapter adapter = new CustomListViewArrayAdapter(this, shareListViewString);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);
                switch (itemPosition){
                    case 0:
                        shareViaMessage();
                        //Toast.makeText(getApplicationContext(), "Message", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        shareViaWhatsApp();
                        //Toast.makeText(getApplicationContext(), "WhatsApp", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        shareViaGmail();
                        //Toast.makeText(getApplicationContext(), "Gmail", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        shareViaClipboard();
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), "No options selected", Toast.LENGTH_LONG).show();
                }
            }

        });

    }

    private void setUpListViewold(final ListView listView){



        listView.setVisibility(View.VISIBLE);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, shareListViewString);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);
                switch (itemPosition){
                    case 0:
                        shareViaMessage();
                        //Toast.makeText(getApplicationContext(), "Message", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        shareViaWhatsApp();
                        //Toast.makeText(getApplicationContext(), "WhatsApp", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        shareViaGmail();
                        //Toast.makeText(getApplicationContext(), "Gmail", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        shareViaClipboard();
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), "No options selected", Toast.LENGTH_LONG).show();
                }
            }

        });

    }

    private void shareViaMessage(){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        //smsIntent.putExtra("address", "12125551212");
        smsIntent.putExtra("sms_body", getMessage());

        startActivity(smsIntent);
    }
    private void shareViaWhatsApp(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setPackage("com.whatsapp");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getMessage());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void shareViaGmail(){
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        //sendIntent.setData(Uri.parse("test@gmail.com"));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
    //  sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "test@gmail.com" });
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Track My Location - Passcode");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getMessage());
        startActivity(sendIntent);
    }

    private String getMessage(){
        return "Click on the link below to receive my location updates.\n"+
                "Link: http://www.trackmylocation.co/"+ codeString;
    }

    private void shareViaClipboard(){
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        //ClipData clip = ClipData.newPlainText("simple text", "Hello, World!");
        clipboard.setText(getMessage());
        Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_LONG).show();
    }

}