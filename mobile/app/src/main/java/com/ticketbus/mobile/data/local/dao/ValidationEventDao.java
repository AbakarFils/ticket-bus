package com.ticketbus.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ticketbus.mobile.data.local.entity.PendingValidation;

import java.util.List;

@Dao
public interface ValidationEventDao {
    @Insert
    void insert(PendingValidation event);

    @Query("SELECT * FROM pending_validations WHERE synced = 0")
    List<PendingValidation> getUnsynced();

    @Query("UPDATE pending_validations SET synced = 1 WHERE id = :id")
    void markSynced(int id);

    @Query("SELECT COUNT(*) FROM pending_validations WHERE synced = 0")
    int getUnsyncedCount();
}
