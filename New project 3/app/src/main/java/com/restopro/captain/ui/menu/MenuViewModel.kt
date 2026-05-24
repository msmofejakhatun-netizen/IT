package com.restopro.captain.ui.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.local.entity.CategoryEntity
import com.restopro.captain.data.local.entity.MenuItemEntity
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import com.restopro.captain.data.repository.MenuRepository
import com.restopro.captain.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val order: OrderEntity? = null,
    val orderItems: List<OrderItemEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val menuItems: List<MenuItemEntity> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val menuRepository: MenuRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val filters = combine(selectedCategory, query) { category, search -> category to search }

    val state: StateFlow<MenuUiState> = combine(
        orderRepository.observeOrder(orderId),
        orderRepository.observeOrderItems(orderId),
        menuRepository.observeCategories(),
        filters.flatMapLatest { (category, search) -> menuRepository.observeItems(category, search) },
        filters
    ) { order, orderItems, categories, items, filter ->
        val (category, search) = filter
        MenuUiState(order, orderItems, categories, items, category, search)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    init {
        viewModelScope.launch { runCatching { menuRepository.refreshMenu() } }
    }

    fun selectCategory(id: String?) {
        selectedCategory.value = id
    }

    fun search(value: String) {
        query.value = value
    }

    fun add(item: MenuItemEntity) = viewModelScope.launch {
        state.value.order?.let { orderRepository.addItem(it, item) }
    }

    fun quantity(itemId: String, quantity: Int) = viewModelScope.launch {
        orderRepository.updateQuantity(itemId, quantity)
    }

    fun sendKot(reprint: Boolean = false) = viewModelScope.launch {
        orderRepository.sendKot(orderId, reprint)
    }

    fun cancelOrder() = viewModelScope.launch {
        orderRepository.cancelOrder(orderId)
    }
}
