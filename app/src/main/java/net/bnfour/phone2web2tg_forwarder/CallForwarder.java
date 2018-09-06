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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class CallForwarder extends BroadcastReceiver{

    static boolean callRinging = false;
    static boolean callReceived = false;
    static String number = null;

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
            if (bundle.containsKey(TelephonyManager.EXTRA_STATE)) {
                String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                // switch requires "constant values", apparently final strings are not worthy
                // we've got a call
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    number = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
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
                        // TODO filtering
                        // slight delay to make sure call log catches up
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            // this should probably never happen
                            // what a terrible failure!
                            Log.wtf("error", "we died :(");
                        }
                        if (isLastCallMissedFrom(number, context)) {
                            String template = preferences.getString("calls_template",
                                    "Missed call from %c");
                            String toSend = template.replace("%c", number);

                            new WebApiSender(endpoint, token).send(toSend);
                        }
                    }
                    // reset the state anyway
                    callReceived = false;
                    callRinging = false;
                    number = null;
                }
            }
        }
    }

    private boolean isLastCallMissedFrom(String number, Context context) {
        try {
            Cursor cursor = context.getContentResolver()
                    .query(CallLog.Calls.CONTENT_URI,
                            null, null, null, CallLog.Calls.DATE + " DESC limit 1;");
            if (cursor.moveToFirst()) {
                int numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                // see if last call was missed, actual code
                boolean missed = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                        == CallLog.Calls.MISSED_TYPE;
                // there is such index (not sure) and last number matches
                String lastNumber = cursor.getString(numberColumnIndex);
                cursor.close();
                return missed && number.equals(lastNumber);
            }

        } catch (SecurityException ex) {
            // we can't check, no forwarding
            return false;
        }
        return false;
    }
}
