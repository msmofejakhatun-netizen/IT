package com.restopro.captain.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.restopro.captain.data.local.dao.MenuDao
import com.restopro.captain.data.local.dao.OrderDao
import com.restopro.captain.data.local.dao.SettingsDao
import com.restopro.captain.data.local.dao.SyncDao
import com.restopro.captain.data.local.dao.TableDao
import com.restopro.captain.data.local.entity.CaptainEntity
import com.restopro.captain.data.local.entity.CategoryEntity
import com.restopro.captain.data.local.entity.MenuItemEntity
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import com.restopro.captain.data.local.entity.PendingSyncEntity
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.local.entity.SettingsEntity

@Database(
    entities = [
        CategoryEntity::class,
        MenuItemEntity::class,
        RestaurantTableEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CaptainEntity::class,
        SettingsEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CaptainDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun tableDao(): TableDao
    abstract fun orderDao(): OrderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun syncDao(): SyncDao
}
