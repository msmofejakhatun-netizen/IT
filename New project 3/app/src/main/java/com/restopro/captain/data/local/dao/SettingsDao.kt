package com.restopro.captain.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restopro.captain.data.local.entity.CaptainEntity
import com.restopro.captain.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCaptain(captain: CaptainEntity)

    @Query("SELECT * FROM captains ORDER BY lastLoginAt DESC LIMIT 1")
    suspend fun lastCaptain(): CaptainEntity?

    @Query("DELETE FROM captains")
    suspend fun clearCaptains()
}
