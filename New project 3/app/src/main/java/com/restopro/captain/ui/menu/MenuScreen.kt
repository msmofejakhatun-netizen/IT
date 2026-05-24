package com.restopro.captain.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.restopro.captain.data.local.entity.MenuItemEntity
import com.restopro.captain.data.local.entity.OrderItemEntity

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text("Order ${state.order?.id?.take(8).orEmpty()}", style = MaterialTheme.typography.titleLarge)
                Text(state.order?.status.orEmpty(), color = MaterialTheme.colorScheme.secondary)
            }
            OutlinedButton(onClick = { viewModel.sendKot(true) }) { Text("Reprint KOT") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.sendKot(false) }, enabled = state.orderItems.isNotEmpty()) { Text("Send KOT") }
        }
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::search,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text("Search menu") }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                AssistChip(onClick = { viewModel.selectCategory(null) }, label = { Text("All") })
            }
            items(state.categories) { category ->
                AssistChip(onClick = { viewModel.selectCategory(category.id) }, label = { Text(category.name) })
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.menuItems, key = { it.id }) { item ->
                    MenuItemRow(item, onAdd = { viewModel.add(item) })
                }
            }
            CartPanel(
                items = state.orderItems,
                total = state.order?.total ?: state.orderItems.sumOf { it.unitPrice * it.quantity },
                onQuantity = viewModel::quantity,
                onKot = { viewModel.sendKot(false) },
                onCancel = viewModel::cancelOrder
            )
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItemEntity, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(if (item.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626), RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                }
                Text("Rs. ${"%.2f".format(item.price)}", color = MaterialTheme.colorScheme.secondary)
                if (item.description.isNotBlank()) Text(item.description, maxLines = 1, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, contentDescription = "Add") }
        }
    }
}

@Composable
private fun CartPanel(
    items: List<OrderItemEntity>,
    total: Double,
    onQuantity: (String, Int) -> Unit,
    onKot: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Cart", style = MaterialTheme.typography.titleMedium)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text("Rs. ${"%.2f".format(item.unitPrice)}", color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { onQuantity(item.id, item.quantity - 1) }) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Decrease")
                    }
                    Text(item.quantity.toString())
                    IconButton(onClick = { onQuantity(item.id, item.quantity + 1) }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Increase")
                    }
                }
            }
        }
        Text("Total Rs. ${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onKot, modifier = Modifier.fillMaxWidth(), enabled = items.isNotEmpty()) { Text("KOT") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)) {
            Text("Cancel Order", color = MaterialTheme.colorScheme.error)
        }
    }
}
