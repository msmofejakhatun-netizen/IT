package com.restopro.captain.ui.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun TablesScreen(
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    viewModel: TablesViewModel = hiltViewModel()
) {
    val tables by viewModel.tables.collectAsState()
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Column {
                Text("Tables", style = MaterialTheme.typography.headlineSmall)
                Text("Hall sections and live table states", color = MaterialTheme.colorScheme.secondary)
            }
        }
        LazyVerticalGrid(columns = GridCells.Adaptive(132.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tables) { table ->
                TableTile(table = table) {
                    scope.launch { onOpenOrder(viewModel.open(table)) }
                }
            }
        }
    }
}

@Composable
private fun TableTile(table: com.restopro.captain.data.local.entity.RestaurantTableEntity, open: () -> Unit) {
    val color = when (table.state) {
        "RUNNING" -> Color(0xFFE0F2FE)
        "RESERVED" -> Color(0xFFFFF7ED)
        "BILLED" -> Color(0xFFE2E8F0)
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .clickable(onClick = open)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(table.name, style = MaterialTheme.typography.titleLarge)
            Text(table.hallName, color = MaterialTheme.colorScheme.secondary)
            Text(table.state)
            Text("Guests ${table.guestCount}  Items ${table.runningItemCount}")
            table.captainName?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}
