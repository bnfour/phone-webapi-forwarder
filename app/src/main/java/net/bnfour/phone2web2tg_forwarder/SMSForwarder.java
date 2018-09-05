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

            if (!enabled || !checker.isTokenValid(token) || !checker.isEndpointValid(endpoint)) {
                // TODO notifications on invalid connection settings?
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

                    boolean filterEnabled = preferences.getBoolean("filter", false);

                    if (filterEnabled) {
                        String filterType = preferences.getString("filter_type", "0");

                        String[] entriesAsArray = preferences.getString("filter_list", "").split(";");

                        // "0" is blacklist
                        if (filterType.equals("0")) {
                            for (String filter : entriesAsArray) {
                                if (sender.equals(filter) || PhoneNumberUtils.compare(sender, filter)) {
                                    return;
                                }
                            }
                        }
                        // "1" (technically, everything that's not "0") is whitelist
                        else {
                            boolean found = false;
                            for (String filter : entriesAsArray) {
                                if (sender.equals(filter) || PhoneNumberUtils.compare(sender, filter)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return;
                            }
                        }
                    }

                    String template = preferences.getString("sms_template", "%s: %t");
                    String toSend = template.replace("%s", sender).replace("%t", message);

                    new WebApiSender(endpoint, token).send(toSend);

                }
            }
        }
    }
}
