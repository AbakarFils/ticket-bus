package com.ticketbus.mobile.data.local.dao

import androidx.room.*
import com.ticketbus.mobile.data.local.entity.PendingValidation

@Dao
interface ValidationEventDao {
    @Insert
    suspend fun insert(event: PendingValidation)
    @Query("SELECT * FROM pending_validations WHERE synced = 0")
    suspend fun getUnsynced(): List<PendingValidation>
    @Query("UPDATE pending_validations SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
    @Query("SELECT COUNT(*) FROM pending_validations WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
}
