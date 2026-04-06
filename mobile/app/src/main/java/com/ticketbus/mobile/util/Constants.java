package com.ticketbus.mobile.util;

public final class Constants {
    private Constants() {}

    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    public static final long SYNC_INTERVAL_MINUTES = 15L;
    public static final String SYNC_WORK_NAME = "ticket_bus_sync";
    public static final String PREF_AUTH_TOKEN = "auth_token";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PUBLIC_KEY = "public_key";
    public static final String PREF_LAST_SYNC = "last_sync";
}
