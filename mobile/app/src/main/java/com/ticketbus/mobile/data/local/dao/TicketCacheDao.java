package com.ticketbus.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ticketbus.mobile.data.local.entity.CachedTicket;

@Dao
public interface TicketCacheDao {
    @Query("SELECT * FROM cached_tickets WHERE ticketNumber = :ticketNumber LIMIT 1")
    CachedTicket findByTicketNumber(String ticketNumber);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachedTicket ticket);

    @Query("DELETE FROM cached_tickets WHERE validatedAt < :cutoff")
    void deleteOlderThan(long cutoff);
}
