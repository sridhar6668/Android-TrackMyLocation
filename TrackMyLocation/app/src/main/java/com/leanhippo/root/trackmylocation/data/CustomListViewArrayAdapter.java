package com.leanhippo.root.trackmylocation.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leanhippo.root.trackmylocation.R;

public class CustomListViewArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public CustomListViewArrayAdapter(Context context, String[] values) {
        super(context, R.layout.custom_list_view_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.custom_list_view_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values[position]);
        // Change the icon for Windows and iPhone
        String s = values[position];
        if (s.startsWith("Message") ) {
            imageView.setImageResource(R.drawable.message);
        }
        else if (s.startsWith("Gmail") ) {
            imageView.setImageResource(R.drawable.gmail);
        }
        else if (s.startsWith("WhatsApp") ) {
            imageView.setImageResource(R.drawable.whatsapp);
        }
        else if (s.startsWith("Copy") ) {
            imageView.setImageResource(R.drawable.clipboard);
        }

        return rowView;
    }
} 