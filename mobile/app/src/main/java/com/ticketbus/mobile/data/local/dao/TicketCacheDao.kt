package com.ticketbus.mobile.data.local.dao

import androidx.room.*
import com.ticketbus.mobile.data.local.entity.CachedTicket

@Dao
interface TicketCacheDao {
    @Query("SELECT * FROM cached_tickets WHERE ticketNumber = :ticketNumber")
    suspend fun findByTicketNumber(ticketNumber: String): CachedTicket?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: CachedTicket)
    @Query("DELETE FROM cached_tickets WHERE validatedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
