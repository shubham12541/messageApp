package com.tominc.buthatke;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SearchEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ListView list;
    ArrayList<MessageThread> threads=  new ArrayList<>();
    GoogleApiClient mGoogleApiClient;

    private int REQUEST_CODE_ASK_PERMISSIONS = 12;
    MessageAdapter adapter;
    FloatingActionButton compose;
    EditText main_search;
    final static int RESOLVE_CONNECTION_REQUEST_CODE=15;
    final static int COMPLETE_AUTHORIZATION_REQUEST_CODE = 17;
    final static int REQUEST_CODE_CREATOR = 18;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mPref = getSharedPreferences("app", MODE_PRIVATE);

        compose = (FloatingActionButton) findViewById(R.id.compose_message);
        list = (ListView) findViewById(R.id.message_list);
        main_search = (EditText) findViewById(R.id.main_search);
        adapter = new MessageAdapter(getApplicationContext(), threads);
        list.setAdapter(adapter);

        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this, ShowMessagesActivity.class);
                MessageThread temp = new MessageThread();
                in.putExtra("thread", temp);
                in.putExtra("type", "new");
                startActivity(in);
            }
        });


        main_search.setTextColor(Color.BLACK);

        main_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED){
            doMainStuff();
        } else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==REQUEST_CODE_ASK_PERMISSIONS){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                doMainStuff();
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "READ_SMS Permission denied", Toast.LENGTH_SHORT)
                        .show();
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doMainStuff(){
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null); // sms/inbox

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                Message msg = new Message();
                String msgData = "";
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
//                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    String tag= cursor.getColumnName(idx);
                    if(tag.equals("address")){
                        msg.setName(cursor.getString(idx));
                    } else if(tag.equals("body")){
                        msg.setMsg(cursor.getString(idx));
                    } else if(tag.equals("date")){
                        msg.setTimestamp(cursor.getString(idx));
                    } else if(tag.equals("type")){
                        String value = cursor.getString(idx);
                        if(value.equals("1")){
                            msg.setIsSend(false);
                        } else if(value.equals("2")){
                            msg.setIsSend(true);
                        }
                    }
                    Log.d("MainActivity", cursor.getColumnName(idx) + " " + cursor.getString(idx));
                }

                boolean flag=true;
                for(int i=0;i<threads.size();i++){
                    if(msg.getName().equals(threads.get(i).getName())){
                        threads.get(i).addMessage(msg);
                        flag=false;
                        break;
                    }
                }
                if(flag){
                    MessageThread thread = new MessageThread();
                    thread.setName(msg.getName());
                    thread.setMsgToShow(msg.getMsg());
                    thread.addMessage(msg);
                    threads.add(thread);
                }
                // use msgData
//                threads.add(msg);

            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
            Toast.makeText(getApplicationContext(), "No Messages Found", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnected(Bundle bundle) {

        String backedup = mPref.getString("backup", null);

        if(backedup==null){
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(driveContentsCallback);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
//            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if(resultCode==RESULT_OK){
                    mGoogleApiClient.connect();
                }
                break;
            case COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // App is authorized, you can go back to sending the API request
                } else {
                    // User denied access, show him the account chooser again
                }
                break;
            case REQUEST_CODE_CREATOR:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d("File created with ID: ", driveId.toString());

                    SharedPreferences.Editor edit = mPref.edit();
                    edit.putString("backup", "yes");
                    edit.commit();

                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.sync_drive){
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(driveContentsCallback);
        }

        return super.onOptionsItemSelected(item);
    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if(!result.getStatus().isSuccess()){
                        Log.e("MainActivity", "failed to create new content");
                        return;
                    }

                    Log.d("MainActivity", "new content created");
                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    byte[] bytearray = null;
                    try {
                        ObjectOutputStream o = new ObjectOutputStream(b);
                        o.writeObject(threads);
                        bytearray = b.toByteArray();
                        outputStream.write(bytearray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setMimeType("text/html").setTitle("Messages").build();
                    IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(result.getDriveContents())
                            .build(mGoogleApiClient);
                    try {
                        startIntentSenderForResult(
                                intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.w("MainActivity", "Unable to send intent", e);
                    }
                }
            };
}


