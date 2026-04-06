package com.ticketbus.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ticketbus.mobile.data.local.dao.*
import com.ticketbus.mobile.data.local.entity.*

@Database(
    entities = [CachedTicket::class, PendingValidation::class, BlacklistEntryLocal::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketCacheDao(): TicketCacheDao
    abstract fun validationEventDao(): ValidationEventDao
    abstract fun blacklistDao(): BlacklistDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ticketbus.db")
                .build().also { INSTANCE = it }
        }
    }
}
