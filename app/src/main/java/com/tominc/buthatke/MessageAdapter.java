package com.tominc.buthatke;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shubham on 24/4/16.
 */
public class MessageAdapter extends BaseAdapter implements Filterable {
    private ArrayList<MessageThread> originalData = null;
    private ArrayList<MessageThread>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    Context c;


    public MessageAdapter(Context c, ArrayList<MessageThread> data){
        this.originalData=data;
        this.filteredData=data;
        mInflater = LayoutInflater.from(c);
        this.c = c;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;


        if (view == null) {
            view = mInflater.inflate(R.layout.message_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.message_item_name);
            holder.msg = (TextView) view.findViewById(R.id.message_item_msg);

            view.setTag(holder);
        } else {

            holder = (ViewHolder) view.getTag();
        }
        holder.title.setText(filteredData.get(i).getName());
//        ArrayList<Message> msgs = filteredData.get(i).getMessages();
//        Message last_msg = msgs.get(0);
        holder.msg.setText(Html.fromHtml(filteredData.get(i).getMsgToShow()));
//        holder.msg.setText(last_msg.getMsg());


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(c, ShowMessagesActivity.class);
                MessageThread temp = filteredData.get(i);
                in.putExtra("thread", temp);
                in.putExtra("type", "old");
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(in);
            }
        });

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    static class ViewHolder{
        TextView title;
        TextView msg;
    }

    private class ItemFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String filterString = charSequence.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<MessageThread> list = originalData;

            int count = list.size();
            final ArrayList<MessageThread> result = new ArrayList<>(count);
            final ArrayList<Message> result_message = new ArrayList<>(count);

            if(false){
                results.values=originalData;
                results.count=originalData.size();
            } else{
                for (MessageThread thread : list){
                    if((thread.getName().toLowerCase()).contains(filterString)){
                        thread.setMsgToShow(thread.getMessages().get(0).getMsg());
                        result.add(thread);
//                    Message temp = thread.getMessages().get(0);
//                    result_message.add(temp);
                    } else{
                        for(Message msg: thread.getMessages()){
                            if((msg.getMsg().toLowerCase()).contains(filterString)){

                                String tobeShown = msg.getMsg().toLowerCase();
                                tobeShown = tobeShown.replace(filterString, "<font color='red'>"+filterString+"</font>");
                                thread.setMsgToShow(tobeShown);
                                result.add(thread);
//                            result_message.add(msg);
                                break;
                            }
                        }
                    }
                }

                results.values = result;
                results.count = result.size();
            }



            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredData = (ArrayList<MessageThread>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
