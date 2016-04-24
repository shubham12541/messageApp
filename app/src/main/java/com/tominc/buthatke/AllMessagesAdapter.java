package com.tominc.buthatke;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shubham on 20/4/16.
 */
public class AllMessagesAdapter extends BaseAdapter {
    ArrayList<MessageThread> threads;
    ArrayList<Message> result_messages;
    Context c;
    int type;

    public AllMessagesAdapter(ArrayList<MessageThread> messages, Context c) {
        this.threads = messages;
        this.c = c;
        this.type=1;
    }

    public AllMessagesAdapter(ArrayList<MessageThread> messages, Context c, ArrayList<Message> result_message){
        this.threads = messages;
        this.result_messages = result_message;
        this.c = c;
        this.type = 2;
    }

    @Override
    public int getCount() {
        return threads.size();
    }

    @Override
    public Object getItem(int i) {
        return threads.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View row = null;
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.message_list_item, viewGroup, false);
        } else{
            row=view;
        }

        TextView name = (TextView) row.findViewById(R.id.message_item_name);
        TextView msg = (TextView) row.findViewById(R.id.message_item_msg);

        name.setText(threads.get(i).getName());

        if(type==1){
            ArrayList<Message> msgs = threads.get(i).getMessages();
            Message last_msg = msgs.get(0);
            msg.setText(last_msg.getMsg());
        } else if(type==2){
            msg.setText(result_messages.get(i).getMsg());

        }

        return row;
    }



}
