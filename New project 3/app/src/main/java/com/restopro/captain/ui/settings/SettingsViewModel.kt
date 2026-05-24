package com.restopro.captain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.repository.SettingsRepository
import com.restopro.captain.printer.BluetoothPrinter
import com.restopro.captain.printer.BluetoothPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val printers: List<BluetoothPrinter> = emptyList(),
    val selectedAddress: String? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val printerManager: BluetoothPrinterManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        refreshPrinters()
    }

    fun refreshPrinters() {
        _state.update { it.copy(printers = printerManager.pairedPrinters()) }
    }

    fun selectPrinter(address: String) = viewModelScope.launch {
        settingsRepository.savePrinter(address)
        _state.update { it.copy(selectedAddress = address, message = "Printer saved") }
    }

    fun testPrint() = viewModelScope.launch {
        val address = state.value.selectedAddress ?: return@launch
        runCatching { printerManager.testPrint(address) }
            .onSuccess { _state.update { it.copy(message = "Test print sent") } }
            .onFailure { error -> _state.update { it.copy(message = error.message ?: "Printer failed") } }
    }
}
