package com.restopro.captain.data.repository

import com.restopro.captain.data.local.entity.CategoryEntity
import com.restopro.captain.data.local.entity.MenuItemEntity
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.remote.dto.CategoryDto
import com.restopro.captain.data.remote.dto.MenuItemDto
import com.restopro.captain.data.remote.dto.OrderItemPayload
import com.restopro.captain.data.remote.dto.OrderPayload
import com.restopro.captain.data.remote.dto.TableDto

fun CategoryDto.toEntity() = CategoryEntity(
    id = id,
    name = name,
    parentId = parentId,
    sortOrder = sortOrder,
    isActive = active,
    updatedAt = updatedAt
)

fun MenuItemDto.toEntity() = MenuItemEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    description = description.orEmpty(),
    price = price,
    taxRate = taxRate ?: 0.0,
    isVeg = veg,
    isAvailable = available,
    imageUrl = imageUrl,
    variantsJson = variantsJson ?: "[]",
    addonsJson = addonsJson ?: "[]",
    updatedAt = updatedAt
)

fun TableDto.toEntity() = RestaurantTableEntity(
    id = id,
    hallId = hallId,
    hallName = hallName,
    name = name,
    state = state,
    guestCount = guestCount,
    captainName = captainName,
    runningItemCount = runningItemCount,
    openedAt = openedAt,
    orderId = orderId,
    updatedAt = updatedAt
)

fun OrderItemEntity.toPayload() = OrderItemPayload(
    id = id,
    menuItemId = menuItemId,
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
    variantName = variantName,
    addonsJson = addonsJson,
    note = note,
    status = status,
    voided = isVoided
)

fun OrderEntity.toPayload(items: List<OrderItemEntity>) = OrderPayload(
    id = id,
    serverId = serverId,
    type = type,
    tableId = tableId,
    status = status,
    guestCount = guestCount,
    notes = notes,
    captainId = captainId,
    captainName = captainName,
    total = total,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items.map { it.toPayload() }
)
