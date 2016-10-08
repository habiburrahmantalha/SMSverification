package com.talha629.smsverification;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SmsActivity extends Activity implements OnItemClickListener {

    private static final int REQUEST_CODE_ASK_PERMISSIONS_READ_SMS = 1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS_INTERNET = 3;
    private final static int NOTIFICATION_ID = 1;
    private static SmsActivity inst;
    private static NotificationManager mNotificationManager;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    Button refreshButton, uploadButton;
    ArrayAdapter arrayAdapter;

    public static SmsActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        smsListView = (ListView) findViewById(R.id.SMSList);

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshSmsInbox();
            }
        });
        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitSMS();
            }
        });
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        checkPermissionReadSMS();
        checkPermissionInternet();

    }

    private void checkPermissionReadSMS() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasSMSReadPermission = checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasSMSReadPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        REQUEST_CODE_ASK_PERMISSIONS_READ_SMS);

            } else
                refreshSmsInbox();

        } else {
            refreshSmsInbox();
        }
    }

    private boolean checkPermissionInternet() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasInternetPermission = checkSelfPermission(Manifest.permission.INTERNET);
            if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, REQUEST_CODE_ASK_PERMISSIONS_INTERNET);
            } else
                return true;
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_INTERNET:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(this, "Internet Permission Granted", Toast.LENGTH_SHORT).show();

                } else {
                    // Permission Denied
                    Toast.makeText(this, "Internet Permission Denied", Toast.LENGTH_SHORT).show();
                }

                break;
            case REQUEST_CODE_ASK_PERMISSIONS_READ_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    refreshSmsInbox();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "SMS_READ Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void refreshSmsInbox() {
        SharedPreferences sharedPref = getSharedPreferences("SMSverification", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        arrayAdapter.clear();

        int i = sharedPref.getInt("sms", 1);
        i--;
        while (i > 0) {
            arrayAdapter.add(sharedPref.getString("sms_" + i, ""));
            i--;
        }
        arrayAdapter.notifyDataSetChanged();
        //editor.putInt("sms",i+1);
        //editor.apply();


        /*
        //Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            String split[] = smsInboxCursor.getString(indexBody).split("\\s+");

            if (split[0].equals("UNILIVER"))
                arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
        */
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
            //checkPermissionSendSMS();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    public void setNotification() {
        Intent notificationIntent = new Intent(this, SmsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                1, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setContentText("Fucking notification")
                .setContentTitle(this.getString(R.string.app_name))
                .setOngoing(true)
                .setSmallIcon(R.drawable.icon_notification)
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
*/

    public void submitSMS() {

        if (!isOnline()) {
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences("SMSverification", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final int i = sharedPref.getInt("sms", 1);
        if (i <= 1)
            return;

        final String sms = sharedPref.getString("sms_" + (i - 1), "");

        String[] separated = sms.split(",");
        String[] piece = separated[1].split(" ");

        if (!piece[0].equals("TEST")) {
            editor.remove("sms_" + (i - 1));
            editor.putInt("sms", i - 1);
            editor.apply();
            return;
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.SUBMIT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(SmsActivity.this, response, Toast.LENGTH_LONG).show();
                        Log.d("response", response);
                        editor.remove("sms_" + (i - 1));
                        editor.putInt("sms", i - 1);
                        editor.apply();
                        refreshSmsInbox();
                        submitSMS();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SmsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("sms", sms);
                params.put("Content-Type", "application/json; charset=utf-8");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}