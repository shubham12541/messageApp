package com.tominc.buthatke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by shubham on 20/4/16.
 */
public class DiscussArrayAdapter extends ArrayAdapter<Message> {

    private TextView countryName;
    private List<Message> countries = new ArrayList<Message>();
    private LinearLayout wrapper;

    @Override
    public void add(Message msg) {
        countries.add(msg);
        sort();
        super.add(msg);
    }

    private void sort(){
        Collections.sort(countries, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return message.getTimestamp().compareTo(t1.getTimestamp());
            }
        });
    }

    @Override
    public void remove(Message object) {
        countries.remove(object);
        super.remove(object);
    }

    public DiscussArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.countries.size();
    }

    public Message getItem(int index) {
        return this.countries.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_message_item, parent, false);
        }

        wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

        Message coment = getItem(position);

        countryName = (TextView) row.findViewById(R.id.comment);

        countryName.setText(coment.getMsg());

        countryName.setBackgroundResource(!coment.isSend() ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        wrapper.setGravity(!coment.isSend() ? Gravity.LEFT : Gravity.RIGHT);

        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
