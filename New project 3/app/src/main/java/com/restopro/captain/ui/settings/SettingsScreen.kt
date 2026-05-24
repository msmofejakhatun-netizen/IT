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
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refreshPrinters()
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 31) permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Column {
                Text("Printer Setup", style = MaterialTheme.typography.headlineSmall)
                Text("Native Bluetooth ESC/POS thermal printers", color = MaterialTheme.colorScheme.secondary)
            }
        }
        Button(onClick = viewModel::refreshPrinters) { Text("Refresh Paired Printers") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.printers, key = { it.address }) { printer ->
                Card(Modifier.fillMaxWidth().clickable { viewModel.selectPrinter(printer.address) }) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Bluetooth, contentDescription = null)
                        Column(Modifier.weight(1f)) {
                            Text(printer.name, fontWeight = FontWeight.SemiBold)
                            Text(printer.address, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (state.selectedAddress == printer.address) Text("Selected", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Button(onClick = viewModel::testPrint, enabled = state.selectedAddress != null) {
            Text("Test Print")
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}
