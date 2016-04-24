package com.tominc.buthatke;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class ShowMessagesActivity extends AppCompatActivity {
    DiscussArrayAdapter adapter;
    private ListView lv;
    private EditText editText1;
    private MessageThread thread;
    Button send_message;

    int REQUEST_CODE_ASK_PERMISSIONS = 12;
    String type;

    String address, message;
    PendingIntent sentPI, deliveredPI;

    LinearLayout number_layout;
    EditText enter_number;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        number_layout = (LinearLayout) findViewById(R.id.add_number_layout);
        enter_number = (EditText) findViewById(R.id.enter_number);

        Intent in = getIntent();
        thread = (MessageThread) in.getSerializableExtra("thread");
        type = in.getStringExtra("type");

        if(type.equals("new")){
            number_layout.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Compose Message");
        } else{
            getSupportActionBar().setTitle(thread.getName());

        }




        send_message = (Button) findViewById(R.id.message_send);
        lv = (ListView) findViewById(R.id.messages);
        lv.setStackFromBottom(true);
        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.single_message_item);

        lv.setAdapter(adapter);

        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(type.equals("new")){
                    if(enter_number.getText().toString().length()!=0){
                        sendMessage();
                    } else{
                        Toast.makeText(getApplicationContext(), "Enter Phone Number", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    sendMessage();
                }

            }
        });

        editText1 = (EditText) findViewById(R.id.enter_message);
        editText1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if (type.equals("new")) {
                        if (enter_number.getText().toString().length() != 0) {
                            sendMessage();
                        } else {
                            Toast.makeText(getApplicationContext(), "Enter Phone Number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        sendMessage();
                    }
                    return true;

                }
                return false;
            }
        });

        addItems();

    }

    private void sendMessage(){

        String temp_msg = editText1.getText().toString();
        if(temp_msg.length()!=0){
            final Message msg = new Message();
            msg.setIsSend(true);
            msg.setMsg(editText1.getText().toString());
            msg.setName(thread.getName());
            Long tsLong=  System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            msg.setTimestamp(ts);

            thread.getMessages().add(msg);

            Log.d("ShowMessageActivity", "sending SMS");
            message = editText1.getText().toString();
            if(type.equals("new")){
                address = enter_number.getText().toString();
            } else{
                address = thread.getName();
            }
            editText1.setText("");

            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED   ";

            sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "Generic failure",
                                    Toast.LENGTH_SHORT).show();
                            adapter.remove(msg);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "No service",
                                    Toast.LENGTH_SHORT).show();
                            adapter.remove(msg);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "Null PDU",
                                    Toast.LENGTH_SHORT).show();
                            adapter.remove(msg);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "Radio off",
                                    Toast.LENGTH_SHORT).show();
                            adapter.remove(msg);
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            //---when the SMS has been delivered---
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS delivered",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(getBaseContext(), "SMS not delivered",
                                    Toast.LENGTH_SHORT).show();
                            adapter.remove(msg);
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED));


            if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.SEND_SMS") == PackageManager.PERMISSION_GRANTED){
                doMainStuff();
            } else{
                ActivityCompat.requestPermissions(ShowMessagesActivity.this, new String[]{"android.permission.SEND_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
            }

            adapter.add(msg);
        } else{
            Toast.makeText(getApplicationContext(), "Message is empty", Toast.LENGTH_SHORT).show();
        }



    }

    private void addItems() {
//        adapter.add(new OneComment(true, "Hello bubbles!"));

        ArrayList<Message> msgs = thread.getMessages();
        for(int i=0;i<msgs.size();i++){
            adapter.add(msgs.get(i));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==REQUEST_CODE_ASK_PERMISSIONS){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                doMainStuff();
            } else {
                // Permission Denied
                Toast.makeText(ShowMessagesActivity.this, "SEND SMS Permission denied", Toast.LENGTH_SHORT)
                        .show();
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doMainStuff(){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(address, null, message, sentPI, deliveredPI);
        message="";

    }

}
