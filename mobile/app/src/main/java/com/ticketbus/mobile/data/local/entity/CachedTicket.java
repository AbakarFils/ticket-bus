package com.ticketbus.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "cached_tickets")
public class CachedTicket {
    @PrimaryKey
    @NonNull
    public String ticketNumber;
    public String nonce;
    public long validatedAt;
    public int validCount;

    public CachedTicket(@NonNull String ticketNumber, String nonce) {
        this.ticketNumber = ticketNumber;
        this.nonce = nonce;
        this.validatedAt = System.currentTimeMillis();
        this.validCount = 1;
    }
}
