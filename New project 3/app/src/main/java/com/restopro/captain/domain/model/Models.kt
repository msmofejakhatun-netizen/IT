package com.restopro.captain.domain.model

enum class TableState { AVAILABLE, RUNNING, RESERVED, BILLED }
enum class OrderType { DINE_IN, TAKEAWAY }
enum class OrderStatus { CART, KOT_SENT, RUNNING, BILLED, CANCELLED }
enum class Role { CAPTAIN, CASHIER, ADMIN }

data class LoginSession(
    val captainId: String,
    val captainName: String,
    val role: Role,
    val restaurantName: String,
    val token: String
)
