package com.restopro.captain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.restopro.captain.ui.dashboard.DashboardScreen
import com.restopro.captain.ui.login.LoginScreen
import com.restopro.captain.ui.menu.MenuScreen
import com.restopro.captain.ui.orders.OrdersScreen
import com.restopro.captain.ui.settings.SettingsScreen
import com.restopro.captain.ui.tables.TablesScreen

@Composable
fun RestoProNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                openTables = { navController.navigate(Routes.TABLES) },
                openOrders = { navController.navigate(Routes.ORDERS) },
                openSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.TABLES) {
            TablesScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { navController.navigate(Routes.menu(it)) }
            )
        }
        composable(
            route = Routes.MENU,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            MenuScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ORDERS) {
            OrdersScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { navController.navigate(Routes.menu(it)) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
