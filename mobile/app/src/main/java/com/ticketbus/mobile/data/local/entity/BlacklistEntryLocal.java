package com.ticketbus.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "blacklist")
public class BlacklistEntryLocal {
    @PrimaryKey
    @NonNull
    public String ticketNumber;
    public String reason;
    public long blacklistedAt;

    public BlacklistEntryLocal(@NonNull String ticketNumber, String reason) {
        this.ticketNumber = ticketNumber;
        this.reason = reason;
        this.blacklistedAt = System.currentTimeMillis();
    }
}
