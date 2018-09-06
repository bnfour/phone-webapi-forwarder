package net.bnfour.phone2web2tg_forwarder;

import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;

public class FilterHelper {
    public static boolean passesFilter(SharedPreferences preferences, String toCheck) {

        boolean filterEnabled = preferences.getBoolean("filter_enabled", false);

        if (filterEnabled) {
            String filterType = preferences.getString("filter_type", "0");

            String[] entriesAsArray = preferences.getString("filter_list", "").split(";");

            // "0" is blacklist
            if (filterType.equals("0")) {
                for (String filter : entriesAsArray) {
                    if (toCheck.equals(filter) || PhoneNumberUtils.compare(toCheck, filter)) {
                        return false;
                    }
                }
                return true;
            }
            // "1" (technically, everything that's not "0") is whitelist
            else {
                boolean found = false;
                for (String filter : entriesAsArray) {
                    if (toCheck.equals(filter) || PhoneNumberUtils.compare(toCheck, filter)) {
                        found = true;
                        break;
                    }
                }
                return found;
            }
        }
        // when filter's disabled, everything passes
        return true;
    }
}
