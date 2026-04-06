package com.ticketbus.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences("ticket_bus_prefs", Context.MODE_PRIVATE);
    }

    public String getAuthToken() { return prefs.getString(Constants.PREF_AUTH_TOKEN, null); }
    public void setAuthToken(String v) { prefs.edit().putString(Constants.PREF_AUTH_TOKEN, v).apply(); }

    public String getUsername() { return prefs.getString(Constants.PREF_USERNAME, null); }
    public void setUsername(String v) { prefs.edit().putString(Constants.PREF_USERNAME, v).apply(); }

    public String getPublicKey() { return prefs.getString(Constants.PREF_PUBLIC_KEY, null); }
    public void setPublicKey(String v) { prefs.edit().putString(Constants.PREF_PUBLIC_KEY, v).apply(); }

    public String getLastSync() { return prefs.getString(Constants.PREF_LAST_SYNC, null); }
    public void setLastSync(String v) { prefs.edit().putString(Constants.PREF_LAST_SYNC, v).apply(); }

    public boolean isLoggedIn() { return getAuthToken() != null; }

    public void clear() { prefs.edit().clear().apply(); }
}
