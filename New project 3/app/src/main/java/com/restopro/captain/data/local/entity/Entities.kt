package com.restopro.captain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val taxRate: Double = 0.0,
    val isVeg: Boolean = true,
    val isAvailable: Boolean = true,
    val imageUrl: String? = null,
    val variantsJson: String = "[]",
    val addonsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurant_tables")
data class RestaurantTableEntity(
    @PrimaryKey val id: String,
    val hallId: String,
    val hallName: String,
    val name: String,
    val state: String,
    val guestCount: Int = 0,
    val captainName: String? = null,
    val runningItemCount: Int = 0,
    val openedAt: Long? = null,
    val orderId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val serverId: String? = null,
    val type: String,
    val tableId: String? = null,
    val status: String,
    val guestCount: Int = 0,
    val notes: String = "",
    val captainId: String,
    val captainName: String,
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDirty: Boolean = true
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantName: String? = null,
    val addonsJson: String = "[]",
    val note: String = "",
    val status: String = "ADDED",
    val isVoided: Boolean = false
)

@Entity(tableName = "captains")
data class CaptainEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val username: String,
    val role: String,
    val token: String,
    val refreshToken: String,
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val restaurantCode: String = "",
    val serverBaseUrl: String = "",
    val rememberLogin: Boolean = false,
    val printerAddress: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val nextAttemptAt: Long = System.currentTimeMillis()
)
