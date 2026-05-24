package com.restopro.captain.ui.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.repository.AuthRepository
import com.restopro.captain.data.repository.OrderRepository
import com.restopro.captain.data.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
    tableRepository: TableRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    val tables: StateFlow<List<RestaurantTableEntity>> =
        tableRepository.observeTables().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun open(table: RestaurantTableEntity): String {
        val session = authRepository.restoreOfflineSession()
        return orderRepository.openOrCreateTableOrder(
            table,
            session?.captainId ?: "offline-captain",
            session?.captainName ?: "Captain"
        )
    }
}
