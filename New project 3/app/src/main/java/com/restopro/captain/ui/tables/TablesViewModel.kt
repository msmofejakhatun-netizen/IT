package com.restopro.captain.ui.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.repository.AuthRepository
import com.restopro.captain.data.repository.OrderRepository
import com.restopro.captain.data.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TableCardUi(
    val table: RestaurantTableEntity,
    val orderAmount: Double,
    val runningMinutes: Long
)

@HiltViewModel
class TablesViewModel @Inject constructor(
    tableRepository: TableRepository,
    orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val orderRepositoryAccess: OrderRepository
) : ViewModel() {
    val tables: StateFlow<List<TableCardUi>> = combine(
        tableRepository.observeTables(),
        orderRepository.observeRunningOrders()
    ) { tables, orders ->
        tables.map { table ->
            val active = orders.firstOrNull { it.tableId == table.id }
            TableCardUi(
                table = table,
                orderAmount = active?.total ?: 0.0,
                runningMinutes = ((System.currentTimeMillis() - (table.openedAt ?: System.currentTimeMillis())) / 60000L).coerceAtLeast(0)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun open(table: RestaurantTableEntity): String {
        val session = authRepository.restoreOfflineSession()
        return orderRepositoryAccess.openOrCreateTableOrder(
            table,
            session?.captainId ?: "offline-captain",
            session?.captainName ?: "Captain"
        )
    }
}
