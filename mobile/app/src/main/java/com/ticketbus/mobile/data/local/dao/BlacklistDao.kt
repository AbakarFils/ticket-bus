package com.ticketbus.mobile.data.local.dao

import androidx.room.*
import com.ticketbus.mobile.data.local.entity.BlacklistEntryLocal

@Dao
interface BlacklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BlacklistEntryLocal>)
    @Query("SELECT * FROM blacklist WHERE ticketNumber = :ticketNumber LIMIT 1")
    suspend fun findByTicketNumber(ticketNumber: String): BlacklistEntryLocal?
    @Query("SELECT COUNT(*) FROM blacklist")
    suspend fun count(): Int
    @Query("DELETE FROM blacklist")
    suspend fun clearAll()
}
