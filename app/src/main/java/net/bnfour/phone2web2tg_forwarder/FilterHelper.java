package net.bnfour.phone2web2tg_forwarder;

import android.content.SharedPreferences;
import android.telephony.PhoneNumberUtils;

public class FilterHelper {

    public static boolean passesFilter(SharedPreferences preferences, String toCheck) {

        boolean filterEnabled = preferences.getBoolean("filter_enabled", false);
        if (filterEnabled) {

            boolean isBlacklist = preferences.getString("filter_type", "0").equals("0");
            String[] entriesAsArray = preferences.getString("filter_list", "").split(";");

            for (String filterEntry : entriesAsArray) {
                // if it's found in the list, the search is over whatever the list type is
                if (toCheck.equals(filterEntry) || PhoneNumberUtils.compare(toCheck, filterEntry)) {
                    // blacklist blocks, whitelist lets to pass through
                    return !isBlacklist;
                }
                // if not in list, pass it for blacklist and block for whitelist
                return isBlacklist;
            }
        }
        // when filter's disabled, everything passes
        return true;
    }
}
