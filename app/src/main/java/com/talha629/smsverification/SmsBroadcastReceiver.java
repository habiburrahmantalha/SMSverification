package com.talha629.smsverification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            if (Build.VERSION.SDK_INT >= 19) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                submitSMS(msgs[0]);
                //inst.updateList(smsMessageStr);
            }
        }
    }

    public void submitSMS(SmsMessage smsMessage) {

        final String smsBody = smsMessage.getMessageBody();
        final String address = smsMessage.getOriginatingAddress();

        String[] separated = smsBody.split(" ");

        if (!separated[0].equals("TEST"))
            return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
        final String date = sdf.format(new Date());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.SUBMIT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                        Log.d("response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        SaveSMSDraft(address + "," + smsBody + "," + date);
                    }
                }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("sms", address + "," + smsBody + "," + date);
                params.put("Content-Type", "application/json; charset=utf-8");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

    void SaveSMSDraft(String str) {

        SharedPreferences sharedPref = mContext.getSharedPreferences("SMSverification", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int i = sharedPref.getInt("sms", 1);
        editor.putString("sms_" + i, str);
        editor.putInt("sms", i + 1);
        editor.apply();

    }

}