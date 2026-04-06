package com.ticketbus.mobile.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ticketbus.mobile.data.local.dao.BlacklistDao;
import com.ticketbus.mobile.data.local.dao.TicketCacheDao;
import com.ticketbus.mobile.data.local.dao.ValidationEventDao;
import com.ticketbus.mobile.data.local.entity.BlacklistEntryLocal;
import com.ticketbus.mobile.data.local.entity.CachedTicket;
import com.ticketbus.mobile.data.local.entity.PendingValidation;

@Database(
    entities = {CachedTicket.class, PendingValidation.class, BlacklistEntryLocal.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TicketCacheDao ticketCacheDao();
    public abstract ValidationEventDao validationEventDao();
    public abstract BlacklistDao blacklistDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "ticketbus.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
