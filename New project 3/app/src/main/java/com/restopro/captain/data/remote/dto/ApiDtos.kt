package com.restopro.captain.data.remote.dto

data class LoginRequest(
    val restaurantCode: String,
    val username: String,
    val password: String
)

data class LoginResponse(
    val captainId: String,
    val captainName: String,
    val username: String,
    val role: String,
    val restaurantId: String,
    val restaurantName: String,
    val token: String,
    val refreshToken: String
)

data class CategoryDto(
    val id: String,
    val name: String,
    val parentId: String?,
    val sortOrder: Int,
    val active: Boolean,
    val updatedAt: Long
)

data class MenuItemDto(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String?,
    val price: Double,
    val taxRate: Double?,
    val veg: Boolean,
    val available: Boolean,
    val imageUrl: String?,
    val variantsJson: String?,
    val addonsJson: String?,
    val updatedAt: Long
)

data class TableDto(
    val id: String,
    val hallId: String,
    val hallName: String,
    val name: String,
    val state: String,
    val guestCount: Int,
    val captainName: String?,
    val runningItemCount: Int,
    val openedAt: Long?,
    val orderId: String?,
    val updatedAt: Long
)

data class OrderItemPayload(
    val id: String,
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantName: String?,
    val addonsJson: String,
    val note: String,
    val status: String,
    val voided: Boolean
)

data class OrderPayload(
    val id: String,
    val serverId: String?,
    val type: String,
    val tableId: String?,
    val status: String,
    val guestCount: Int,
    val notes: String,
    val captainId: String,
    val captainName: String,
    val total: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val items: List<OrderItemPayload>
)

data class KotRequest(val orderId: String, val reprint: Boolean = false)
data class SyncEnvelope(val operation: String, val entityType: String, val payloadJson: String)
data class SyncResult(val accepted: Boolean, val serverId: String?, val message: String?)
data class HealthResponse(val ok: Boolean, val restaurantName: String?)
