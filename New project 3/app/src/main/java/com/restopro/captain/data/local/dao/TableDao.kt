package com.restopro.captain.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM restaurant_tables ORDER BY hallName, name")
    fun observeTables(): Flow<List<RestaurantTableEntity>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id LIMIT 1")
    suspend fun getTable(id: String): RestaurantTableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTables(tables: List<RestaurantTableEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTable(table: RestaurantTableEntity)
}
