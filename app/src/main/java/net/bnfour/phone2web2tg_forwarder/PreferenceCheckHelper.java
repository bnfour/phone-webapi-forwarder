package net.bnfour.phone2web2tg_forwarder;

import android.content.Context;

import java.util.regex.Pattern;

public class PreferenceCheckHelper {

    private Context _context;

    public PreferenceCheckHelper(Context context) {
        _context = context;
    }

    public boolean isTokenValid(String token) {
        String tokenPattern = _context.getResources().getString(R.string.token_regex);
        Pattern pattern = Pattern.compile(tokenPattern);
        return pattern.matcher(token).matches();
    }

    public boolean isEndpointValid(String endpoint) {
        // this is not complete, i know
        return endpoint.startsWith("http") && endpoint.endsWith("/");
    }
}
