package com.restopro.captain.navigation

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val TABLES = "tables"
    const val ORDERS = "orders"
    const val SETTINGS = "settings"
    const val MENU = "menu/{orderId}"

    fun menu(orderId: String) = "menu/$orderId"
}
