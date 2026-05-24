package com.restopro.captain.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restopro.captain.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM pending_sync ORDER BY createdAt")
    fun observeQueue(): Flow<List<PendingSyncEntity>>

    @Query("SELECT * FROM pending_sync WHERE nextAttemptAt <= :now ORDER BY createdAt LIMIT :limit")
    suspend fun due(now: Long = System.currentTimeMillis(), limit: Int = 50): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: PendingSyncEntity)

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun remove(id: String)

    @Query("UPDATE pending_sync SET retryCount = retryCount + 1, lastError = :error, nextAttemptAt = :nextAttemptAt WHERE id = :id")
    suspend fun markFailed(id: String, error: String, nextAttemptAt: Long)
}
