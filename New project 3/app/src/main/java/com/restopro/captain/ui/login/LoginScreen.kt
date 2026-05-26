package com.restopro.captain.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }
    LaunchedEffect(state.snack) {
        state.snack?.let { snack.showSnackbar(it); viewModel.consumeSnack() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("RestoPro Captain", style = MaterialTheme.typography.headlineMedium)
        Text("Native LAN ordering terminal", color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(24.dp))
        SnackbarHost(hostState = snack)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.restaurantCode,
                    onValueChange = { value -> viewModel.update { copy(restaurantCode = value.uppercase()) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Restaurant Code") }
                )
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { value -> viewModel.update { copy(username = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { value -> viewModel.update { copy(password = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = state.serverIp,
                    onValueChange = { value -> viewModel.update { copy(serverIp = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Local Server IP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.remember,
                        onCheckedChange = { checked -> viewModel.update { copy(remember = checked) } }
                    )
                    Text("Remember Login")
                    Spacer(Modifier.weight(1f))
                    state.connectionOk?.let {
                        Text(if (it) "LAN OK" else "Offline", color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        state.latencyMs?.let { ms -> Text("  ${ms}ms", color = MaterialTheme.colorScheme.secondary) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = viewModel::detectServer, modifier = Modifier.weight(1f), enabled = !state.loading) {
                        Icon(Icons.Outlined.Lan, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Detect")
                    }
                    OutlinedButton(onClick = viewModel::testConnection, modifier = Modifier.weight(1f), enabled = !state.loading) {
                        Text("Test LAN")
                    }
                }
                Button(onClick = viewModel::login, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) {
                    if (state.loading) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Login")
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
