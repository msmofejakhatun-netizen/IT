package com.restopro.captain.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.restopro.captain.data.local.entity.CategoryEntity
import com.restopro.captain.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder, name")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM menu_items WHERE isAvailable = 1 ORDER BY name")
    fun observeMenuItems(): Flow<List<MenuItemEntity>>

    @Query("""
        SELECT * FROM menu_items
        WHERE isAvailable = 1
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:query = '' OR name LIKE '%' || :query || '%')
        ORDER BY name
    """)
    fun observeMenuItems(categoryId: String?, query: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMenuItems(items: List<MenuItemEntity>)
}
