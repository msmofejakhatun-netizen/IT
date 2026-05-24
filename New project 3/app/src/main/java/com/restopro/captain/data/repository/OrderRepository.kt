package com.restopro.captain.data.repository

import com.google.gson.Gson
import com.restopro.captain.data.local.dao.OrderDao
import com.restopro.captain.data.local.dao.SyncDao
import com.restopro.captain.data.local.dao.TableDao
import com.restopro.captain.data.local.entity.MenuItemEntity
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import com.restopro.captain.data.local.entity.PendingSyncEntity
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.remote.api.KotApi
import com.restopro.captain.data.remote.api.OrderApi
import com.restopro.captain.data.remote.dto.KotRequest
import com.restopro.captain.domain.model.OrderStatus
import com.restopro.captain.domain.model.OrderType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val tableDao: TableDao,
    private val syncDao: SyncDao,
    private val orderApi: OrderApi,
    private val kotApi: KotApi,
    private val gson: Gson
) {
    fun observeRunningOrders() = orderDao.observeRunningOrders()
    fun observeOrder(orderId: String) = orderDao.observeOrder(orderId)
    fun observeOrderItems(orderId: String) = orderDao.observeOrderItems(orderId)

    suspend fun createTakeawayOrder(captainId: String, captainName: String): String {
        val order = OrderEntity(
            id = UUID.randomUUID().toString(),
            type = OrderType.TAKEAWAY.name,
            tableId = null,
            status = OrderStatus.CART.name,
            captainId = captainId,
            captainName = captainName
        )
        orderDao.upsertOrder(order)
        enqueue("UPSERT_ORDER", order.id, gson.toJson(order))
        return order.id
    }

    suspend fun openOrCreateTableOrder(
        table: RestaurantTableEntity,
        captainId: String,
        captainName: String
    ): String {
        val active = orderDao.activeOrderForTable(table.id)
        if (active != null) return active.id
        val orderId = UUID.randomUUID().toString()
        val order = OrderEntity(
            id = orderId,
            type = OrderType.DINE_IN.name,
            tableId = table.id,
            status = OrderStatus.CART.name,
            guestCount = table.guestCount.coerceAtLeast(1),
            captainId = captainId,
            captainName = captainName
        )
        orderDao.upsertOrder(order)
        tableDao.upsertTable(
            table.copy(
                state = "RUNNING",
                captainName = captainName,
                openedAt = System.currentTimeMillis(),
                orderId = orderId
            )
        )
        enqueue("UPSERT_ORDER", order.id, gson.toJson(order))
        return orderId
    }

    suspend fun addItem(order: OrderEntity, item: MenuItemEntity, note: String = "") {
        val orderItem = OrderItemEntity(
            id = UUID.randomUUID().toString(),
            orderId = order.id,
            menuItemId = item.id,
            name = item.name,
            quantity = 1,
            unitPrice = item.price,
            note = note
        )
        val updated = order.copy(
            total = order.total + item.price,
            isDirty = true,
            updatedAt = System.currentTimeMillis()
        )
        orderDao.saveOrderWithItems(updated, listOf(orderItem))
        enqueue("UPSERT_ORDER", updated.id, gson.toJson(updated))
    }

    suspend fun updateQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) orderDao.voidItem(itemId) else orderDao.updateQuantity(itemId, quantity)
    }

    suspend fun addOrderNote(orderId: String, note: String) {
        orderDao.updateNotes(orderId, note)
        enqueue("ORDER_NOTE", orderId, """{"orderId":"$orderId","note":${gson.toJson(note)}}""")
    }

    suspend fun addItemNote(itemId: String, note: String) {
        orderDao.updateItemNote(itemId, note)
        enqueue("ITEM_NOTE", itemId, """{"itemId":"$itemId","note":${gson.toJson(note)}}""")
    }

    suspend fun voidItem(itemId: String) {
        orderDao.voidItem(itemId)
        enqueue("VOID_ITEM", itemId, """{"itemId":"$itemId"}""")
    }

    suspend fun mergeTables(sourceTableId: String, targetTableId: String) {
        val sourceOrder = orderDao.activeOrderForTable(sourceTableId) ?: return
        orderDao.updateTable(sourceOrder.id, targetTableId)
        val sourceTable = tableDao.getTable(sourceTableId)
        val targetTable = tableDao.getTable(targetTableId)
        if (sourceTable != null) tableDao.upsertTable(sourceTable.copy(state = "AVAILABLE", orderId = null, runningItemCount = 0))
        if (targetTable != null) tableDao.upsertTable(targetTable.copy(state = "RUNNING", orderId = sourceOrder.id))
        enqueue("MERGE_TABLE", sourceOrder.id, """{"sourceTableId":"$sourceTableId","targetTableId":"$targetTableId"}""")
    }

    suspend fun splitBill(orderId: String, itemIds: Set<String>, captainId: String, captainName: String): String? {
        val order = orderDao.getOrder(orderId) ?: return null
        val items = orderDao.itemsForOrder(orderId).filter { it.id in itemIds }
        if (items.isEmpty()) return null
        val splitOrderId = UUID.randomUUID().toString()
        val splitItems = items.map { it.copy(id = UUID.randomUUID().toString(), orderId = splitOrderId) }
        val splitOrder = order.copy(
            id = splitOrderId,
            serverId = null,
            captainId = captainId,
            captainName = captainName,
            total = splitItems.sumOf { it.unitPrice * it.quantity },
            isDirty = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        orderDao.saveOrderWithItems(splitOrder, splitItems)
        items.forEach { orderDao.voidItem(it.id) }
        enqueue("SPLIT_BILL", orderId, gson.toJson(mapOf("orderId" to orderId, "splitOrderId" to splitOrderId, "itemIds" to itemIds)))
        return splitOrderId
    }

    suspend fun sendKot(orderId: String, reprint: Boolean = false) {
        val result = runCatching { kotApi.sendKot(KotRequest(orderId, reprint)) }
        if (result.isSuccess) {
            orderDao.updateStatus(orderId, OrderStatus.KOT_SENT.name)
        } else {
            enqueue("SEND_KOT", orderId, gson.toJson(KotRequest(orderId, reprint)))
        }
    }

    suspend fun cancelOrder(orderId: String) {
        orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
        runCatching { orderApi.cancelOrder(mapOf("orderId" to orderId)) }
            .onFailure { enqueue("CANCEL_ORDER", orderId, """{"orderId":"$orderId"}""") }
    }

    suspend fun markBilled(orderId: String) {
        orderDao.updateStatus(orderId, OrderStatus.BILLED.name)
        enqueue("BILL_ORDER", orderId, """{"orderId":"$orderId"}""")
    }

    private suspend fun enqueue(operation: String, entityId: String, payload: String) {
        syncDao.enqueue(
            PendingSyncEntity(
                id = UUID.randomUUID().toString(),
                entityType = "ORDER",
                entityId = entityId,
                operation = operation,
                payloadJson = payload
            )
        )
    }
}
