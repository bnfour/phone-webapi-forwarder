package net.bnfour.phone2web2tg_forwarder;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static List<String> keysDependentOnConnection = new ArrayList<>(
            Arrays.asList("filter_enabled", "sms_enabled", "calls_enabled")
    );
    // only used to instantiate PreferenceCheckHelper and Toast
    private Context _context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getActivity().getApplicationContext();
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

    // i'm bad at computer programming

    // true when token is ok, also sets summary
    private boolean updateToken() {
        final String key = "api_token";
        String token = getPreferenceManager().getSharedPreferences().getString(key, "");
        Preference p = findPreference(key);
        PreferenceCheckHelper helper = new PreferenceCheckHelper(_context);

        if (token.equals("")) {
            p.setSummary(R.string.connection_not_set);
            return false;
        } else if (!helper.isTokenValid(token)) {
            p.setSummary(R.string.bad_token);
            return false;
        } else {
            p.setSummary(R.string.connection_set_okay);
            return true;
        }
    }

    // true when endpoint is 'ok', also sets summary
    private boolean updateEndpoint() {
        final String key = "api_endpoint_url";
        String endpoint = getPreferenceManager().getSharedPreferences().getString(key, "");
        Preference p = findPreference(key);
        PreferenceCheckHelper helper = new PreferenceCheckHelper(_context);

        if (endpoint.equals("")) {
            p.setSummary(R.string.connection_not_set);
            return false;
        } else if (!helper.isEndpointValid(endpoint)) {
            p.setSummary(R.string.bad_endpoint);
            return false;
        } else {
            p.setSummary(R.string.connection_set_okay);
            return true;
        }
    }

    private void updateConnectionAndDependent() {
        // &, not &&, since we have side effects
        boolean valid = updateToken() & updateEndpoint();

        if (!valid) {
            Toast.makeText(_context, getString(R.string.bad_connection_toast), Toast.LENGTH_LONG).show();
        }

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
