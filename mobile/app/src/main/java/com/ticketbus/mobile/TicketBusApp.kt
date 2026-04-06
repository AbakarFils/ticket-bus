package com.ticketbus.mobile

import android.app.Application
import androidx.work.*
import com.ticketbus.mobile.offline.SyncWorker
import java.util.concurrent.TimeUnit

class TicketBusApp : Application() {
    companion object { lateinit var instance: TicketBusApp }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
