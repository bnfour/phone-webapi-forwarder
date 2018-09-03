package net.bnfour.phone2web2tg_forwarder;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static List<String> keysDependentOnConnection = new ArrayList<>(
            Arrays.asList("filter_enabled", "sms_enabled", "calls_enabled")
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        updateAll();
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        updateAll();
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateAll();
    }

    // also updates summaries
    // i'm bad at computer programming
    private boolean isSetToValue(String key) {
        Preference p = findPreference(key);
        String value = getPreferenceManager().getSharedPreferences().getString(key, "");
        boolean valid = !value.equals("");
        p.setSummary(valid ? value : getString(R.string.connection_not_set));
        return valid;
    }

    private void updateConnectionAndDependent() {
        boolean valid = isSetToValue("api_token") & isSetToValue("api_endpoint_url");
        for (String depKey: keysDependentOnConnection) {
            Preference p = findPreference(depKey);
            p.setEnabled(valid);
        }
    }


    private void updateAll() {
        updateConnectionAndDependent();
        updateFilterList();
        updateListType();
    }

    // the following was taken straight from sms forwarder

    private void updateListType() {
        final String listTypeKey = "filter_type";

        Preference listType = findPreference(listTypeKey);

        int type = Integer.parseInt(getPreferenceManager()
                .getSharedPreferences().getString(listTypeKey, "0"));
        String summary = type == 0 ? getString(R.string.filter_type_summary_black) :
                getString(R.string.filter_type_summary_white);

        listType.setSummary(summary);
    }

    private void updateFilterList() {
        final String filterKey = "filter_list";

        Preference filterList = findPreference(filterKey);

        String value = getPreferenceManager()
                .getSharedPreferences().getString(filterKey, "");
        // split of empty line gives one empty line, but there are no entries
        int count = value.equals("") ? 0 : value.split(";").length;

        String summary = getString(R.string.filter_list_entry_number) + String.valueOf(count);
        filterList.setSummary(summary);
    }
}
