package com.leanhippo.root.trackmylocation;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;



public class ShareWithContacts extends ActionBarActivity {

    ArrayList<String> phoneNumberList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_with_contacts);


        String[] str={"Andoid","Jelly Bean","Froyo",
                "Ginger Bread","Eclipse Indigo","Eclipse Juno"};

       getPhoneNumbers();
        MultiAutoCompleteTextView mt = (MultiAutoCompleteTextView)
                findViewById(R.id.contactSearchMultiAutoCompleteTextView);

        mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ArrayAdapter<String> adp=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,phoneNumberList);

        mt.setThreshold(1);
        mt.setAdapter(adp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_with_contacts, menu);
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

    public void getPhoneNumbers() {

        Cursor phones = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
         //   System.out.println(".................."+phoneNumber);
            phoneNumberList.add(phoneNumber);
        }
        phones.close();
    }
}
