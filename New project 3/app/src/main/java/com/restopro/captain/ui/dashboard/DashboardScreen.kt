package com.restopro.captain.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    openTables: () -> Unit,
    openOrders: () -> Unit,
    openSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(state.restaurantName.ifBlank { "RestoPro" }, style = MaterialTheme.typography.headlineSmall)
                Text(state.captainName.ifBlank { "Captain" }, color = MaterialTheme.colorScheme.secondary)
            }
            Text(if (state.socketConnected) "Realtime" else "Offline queue", color = if (state.socketConnected) MaterialTheme.colorScheme.primary else Color(0xFFF97316))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Today's Sales", "₹${"%.0f".format(state.todaySales)}", Modifier.weight(1f))
            MetricCard("Running Orders", state.runningOrders.toString(), Modifier.weight(1f))
            MetricCard("Pending KOT", state.pendingKot.toString(), Modifier.weight(1f))
        }
        Text("Shift ${state.shiftStatus} • Active Tables ${state.occupiedTables}", color = MaterialTheme.colorScheme.secondary)
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
        val actions = buildList {
            add(Action("Tables", Icons.Outlined.TableRestaurant, openTables))
            add(Action("Running Orders", Icons.Outlined.RestaurantMenu, openOrders))
            if (state.role != "CAPTAIN") {
                add(Action("Printer", Icons.Outlined.Print, openSettings))
                add(Action("Settings", Icons.Outlined.Tune, openSettings))
            }
        }
        LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(actions) { action ->
                Button(onClick = action.open, modifier = Modifier.height(82.dp).fillMaxWidth()) {
                    Icon(action.icon, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(action.label)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(96.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private data class Action(val label: String, val icon: ImageVector, val open: () -> Unit)
