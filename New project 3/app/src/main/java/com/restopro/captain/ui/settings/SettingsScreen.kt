package com.restopro.captain.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.refreshPrinters() }
    LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= 31) permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT) }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Column {
                Text("Enterprise Printer Setup", style = MaterialTheme.typography.headlineSmall)
                Text("Bluetooth / USB / LAN", color = MaterialTheme.colorScheme.secondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = state.lanAddress, onValueChange = viewModel::setLanAddress, label = { Text("LAN host:port") }, modifier = Modifier.weight(1f))
            Button(onClick = viewModel::addLanPrinter) { Text("Add LAN") }
        }
        Button(onClick = viewModel::refreshPrinters) { Text("Discover Printers") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.printers, key = { it.id }) { printer ->
                Card(Modifier.fillMaxWidth().clickable { viewModel.selectPrinter(printer) }) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Bluetooth, contentDescription = null)
                        Column(Modifier.weight(1f)) {
                            Text(printer.name, fontWeight = FontWeight.SemiBold)
                            Text("${printer.transport} • ${printer.address}", color = MaterialTheme.colorScheme.secondary)
                        }
                        if (state.selected?.id == printer.id) Text("Selected", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::testPrint, enabled = state.selected != null, modifier = Modifier.weight(1f)) { Text("Test") }
            Button(onClick = viewModel::healthCheck, enabled = state.selected != null, modifier = Modifier.weight(1f)) { Text("Health") }
            Button(onClick = viewModel::retryQueue, modifier = Modifier.weight(1f)) { Text("Retry Queue") }
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}
