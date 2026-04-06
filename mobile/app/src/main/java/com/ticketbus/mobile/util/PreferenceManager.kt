package com.ticketbus.mobile.util

import android.content.Context

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("ticket_bus_prefs", Context.MODE_PRIVATE)
    var authToken: String?
        get() = prefs.getString(Constants.PREF_AUTH_TOKEN, null)
        set(v) = prefs.edit().putString(Constants.PREF_AUTH_TOKEN, v).apply()
    var username: String?
        get() = prefs.getString(Constants.PREF_USERNAME, null)
        set(v) = prefs.edit().putString(Constants.PREF_USERNAME, v).apply()
    var publicKey: String?
        get() = prefs.getString(Constants.PREF_PUBLIC_KEY, null)
        set(v) = prefs.edit().putString(Constants.PREF_PUBLIC_KEY, v).apply()
    var lastSync: String?
        get() = prefs.getString(Constants.PREF_LAST_SYNC, null)
        set(v) = prefs.edit().putString(Constants.PREF_LAST_SYNC, v).apply()
    fun isLoggedIn(): Boolean = authToken != null
    fun clear() = prefs.edit().clear().apply()
}
