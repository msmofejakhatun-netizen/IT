package com.restopro.captain.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.RestaurantTableEntity
import com.restopro.captain.data.repository.AuthRepository
import com.restopro.captain.data.repository.OrderRepository
import com.restopro.captain.data.repository.TableRepository
import com.restopro.captain.domain.usecase.RefreshOperationalDataUseCase
import com.restopro.captain.socket.SocketManager
import com.restopro.captain.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val captainName: String = "",
    val restaurantName: String = "",
    val role: String = "CAPTAIN",
    val runningOrders: Int = 0,
    val occupiedTables: Int = 0,
    val pendingKot: Int = 0,
    val socketConnected: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    orderRepository: OrderRepository,
    tableRepository: TableRepository,
    socketManager: SocketManager,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val refreshOperationalData: RefreshOperationalDataUseCase
) : ViewModel() {
    private val session = MutableStateFlow(Triple("", "", "CAPTAIN"))
    val state: StateFlow<DashboardUiState> = combine(
        orderRepository.observeRunningOrders(),
        tableRepository.observeTables(),
        socketManager.connected,
        session
    ) { orders: List<OrderEntity>, tables: List<RestaurantTableEntity>, connected: Boolean, session: Triple<String, String, String> ->
        DashboardUiState(
            captainName = session.first,
            restaurantName = session.second,
            role = session.third,
            runningOrders = orders.size,
            occupiedTables = tables.count { it.state == "RUNNING" },
            pendingKot = orders.count { it.status == "CART" || it.status == "KOT_SENT" },
            socketConnected = connected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        viewModelScope.launch {
            authRepository.restoreOfflineSession()?.let { session.value = Triple(it.captainName, it.restaurantName, it.role.name) }
            socketManager.connect()
            syncManager.start()
            runCatching { refreshOperationalData() }
        }
    }
}
