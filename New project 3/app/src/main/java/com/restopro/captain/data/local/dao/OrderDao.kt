package com.restopro.captain.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE status NOT IN ('BILLED', 'CANCELLED') ORDER BY updatedAt DESC")
    fun observeRunningOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun observeOrder(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrder(orderId: String): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND isVoided = 0 ORDER BY name")
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND isVoided = 0 ORDER BY name")
    suspend fun itemsForOrder(orderId: String): List<OrderItemEntity>

    @Query("SELECT * FROM orders WHERE tableId = :tableId AND status NOT IN ('BILLED', 'CANCELLED') LIMIT 1")
    suspend fun activeOrderForTable(tableId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE isDirty = 1 ORDER BY updatedAt LIMIT :limit")
    suspend fun dirtyOrders(limit: Int = 25): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET isDirty = 0, serverId = :serverId, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun markSynced(orderId: String, serverId: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE order_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: String, quantity: Int)

    @Query("UPDATE order_items SET note = :note WHERE id = :itemId")
    suspend fun updateItemNote(itemId: String, note: String)

    @Query("UPDATE order_items SET isVoided = 1, status = 'VOIDED' WHERE id = :itemId")
    suspend fun voidItem(itemId: String)

    @Query("UPDATE orders SET status = :status, isDirty = 1, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET tableId = :tableId, isDirty = 1, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateTable(orderId: String, tableId: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET notes = :notes, isDirty = 1, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateNotes(orderId: String, notes: String, updatedAt: Long = System.currentTimeMillis())

    @Transaction
    suspend fun saveOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        upsertOrder(order)
        upsertItems(items)
    }
}
