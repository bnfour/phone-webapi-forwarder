package net.bnfour.phone2web2tg_forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

import java.util.HashMap;
import java.util.Map;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

// this is hell of a mess
// but hey -- at least i got this working

public class SMSForwarder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

            Context appContext = context.getApplicationContext();

            SharedPreferences preferences = getDefaultSharedPreferences(appContext);

            boolean enabled = preferences.getBoolean("sms_enabled", false);
            String endpoint = preferences.getString("api_endpoint_url", "");
            String token = preferences.getString("api_token", "");

            PreferenceCheckHelper checker = new PreferenceCheckHelper(appContext);

            if (!enabled) {
                return;
            }
            if (!checker.isTokenValid(token) || !checker.isEndpointValid(endpoint)) {
                Notifier.showNotification(appContext, appContext.getString(R.string.bad_connection_notification));
                return;
            }

            Bundle bundle = intent.getExtras();
            if (bundle.containsKey("pdus")) {

                // here we build a dict where keys are message senders
                // all pdu messages from one sender are combined to one long string
                // (taken straight from SMS Forwarder yet again)
                Map<String, String> messages = new HashMap<String, String>();

                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = msg.getOriginatingAddress();
                    String text = msg.getMessageBody();

                    if (messages.containsKey(sender)) {
                        String newText = messages.get(sender) + text;
                        messages.put(sender, newText);
                    } else {
                        messages.put(sender, text);
                    }
                }
                // every message in a dict is checked against filters
                // and is forwarded if it matches
                for (String sender: messages.keySet()) {

                    String message = messages.get(sender);

                    if (!FilterHelper.passesFilter(preferences, sender)) {
                        return;
                    }

                    String template = preferences.getString("sms_template", "%s: %t");
                    String toSend = template.replace("%s", sender).replace("%t", message);

                    new WebApiSender(appContext, endpoint, token).send(toSend);

                }
            }
        }
    }
}
