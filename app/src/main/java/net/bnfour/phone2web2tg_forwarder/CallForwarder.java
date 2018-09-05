package net.bnfour.phone2web2tg_forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class CallForwarder extends BroadcastReceiver implements Callback<Response> {

    static boolean callRinging = false;
    static boolean callReceived = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            Context appContext = context.getApplicationContext();

            SharedPreferences preferences = getDefaultSharedPreferences(appContext);

            boolean enabled = preferences.getBoolean("calls_enabled", false);
            String endpoint = preferences.getString("api_endpoint_url", "");
            String token = preferences.getString("api_token", "");

            PreferenceCheckHelper checker = new PreferenceCheckHelper(appContext);

            if (!enabled || !checker.isTokenValid(token) || !checker.isEndpointValid(endpoint)) {
                // TODO notifications on invalid connection settings?
                return;
            }

            Bundle bundle = intent.getExtras();
            if (bundle.containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    && bundle.containsKey(TelephonyManager.EXTRA_STATE)) {
                String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                // switch requires "constant values", apparently final strings are not worthy
                // we've got a call
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    callRinging = true;
                // the call was answered
                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    callReceived = true;
                // we're done with this call
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    // if we have an unanswered call
                    if (callRinging && !callReceived) {
                        // it might be a dismissed call
                        // to know for sure, we need to check call log --
                        // if last call is 'missed' that should be the call that was tracked here

                        // slight delay to make sure call log catches up
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            // this should probably never happen
                            // what a terrible failure!
                            Log.wtf("error", "we died :(");
                        }

                        String number = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        if (isLastCallMissedFrom(number, context)) {
                            String template = preferences.getString("calls_template",
                                    "Missed call from %c");
                            String toSend = template.replace("%c", number);
                            send(endpoint, token, toSend);
                        }
                    }
                    // reset the flags anyway
                    callReceived = false;
                    callRinging = false;
                }
            }
        }
    }

    private boolean isLastCallMissedFrom(String number, Context context) {
        try {
            Cursor cursor = context.getContentResolver()
                    .query(CallLog.Calls.CONTENT_URI,
                            null, null, null, CallLog.Calls.DATE + "DESC limit 1;");
            if (cursor.moveToFirst()) {
                int numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                // this is crap like seen on stackoverflow, but with missed type
                int typeColumnIndex = cursor.getColumnIndex(String.valueOf(CallLog.Calls.MISSED_TYPE));
                // there is such index (not sure) and last number matches
                return (typeColumnIndex != -1) && number.equals(cursor.getString(numberColumnIndex));
            }

        } catch (SecurityException ex) {
            // we can't check, no forwarding
            return false;
        }
        return false;
    }

    // TODO move these both from here and from SMSForwarder to another class

    private void send(String endpoint, String token, String message) {
        Request request = new Request(token, message);

        // Retrofit stuff
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        IDontnetTelegramForwarderApi api
                = retrofit.create(IDontnetTelegramForwarderApi.class);

        Call<Response> call = api.sendRequest(request);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
        if (response.isSuccessful()) {
            Response result = response.body();
            if (!result.ok) {
                // TODO retrying, notification
                Log.d("result", "not ok");
            } else {
                Log.d("result", "ok");
            }
        } else {
            // TODO notification about failure
            try {
                Log.d("result", "not success " + response.errorBody().string());
            } catch (Exception ex) {}
        }
    }

    @Override
    public void onFailure(Call<Response> call, Throwable t) {
        // TODO notification
        Log.d("rip", t.toString());
    }
}
