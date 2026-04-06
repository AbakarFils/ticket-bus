package com.ticketbus.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ticketbus.mobile.data.local.entity.BlacklistEntryLocal;

import java.util.List;

@Dao
public interface BlacklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BlacklistEntryLocal> entries);

    @Query("SELECT * FROM blacklist WHERE ticketNumber = :ticketNumber LIMIT 1")
    BlacklistEntryLocal findByTicketNumber(String ticketNumber);

    @Query("SELECT COUNT(*) FROM blacklist")
    int count();

    @Query("DELETE FROM blacklist")
    void clearAll();
}
