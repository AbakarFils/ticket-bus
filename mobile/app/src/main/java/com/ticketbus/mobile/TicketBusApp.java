package com.ticketbus.mobile;

import android.app.Application;

public class TicketBusApp extends Application {
    private static TicketBusApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static TicketBusApp getInstance() {
        return instance;
    }
}
