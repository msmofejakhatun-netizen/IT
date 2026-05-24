package com.restopro.captain.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    orderRepository: OrderRepository
) : ViewModel() {
    val orders: StateFlow<List<OrderEntity>> =
        orderRepository.observeRunningOrders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
