package com.restopro.captain.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.repository.SettingsRepository
import com.restopro.captain.printer.PaperWidth
import com.restopro.captain.printer.PrinterEngine
import com.restopro.captain.printer.PrinterTarget
import com.restopro.captain.printer.PrinterTransportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val printers: List<PrinterTarget> = emptyList(),
    val selected: PrinterTarget? = null,
    val lanAddress: String = "",
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val printerEngine: PrinterEngine,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init { refreshPrinters() }

    fun refreshPrinters() = viewModelScope.launch {
        _state.update { it.copy(printers = printerEngine.discoverAll()) }
    }

    fun setLanAddress(value: String) { _state.update { it.copy(lanAddress = value) } }

    fun addLanPrinter() {
        val addr = state.value.lanAddress.trim()
        if (addr.isBlank()) return
        val lan = PrinterTarget(id = "lan:$addr", name = "LAN Printer", transport = PrinterTransportType.LAN, address = addr, paperWidth = PaperWidth.MM80)
        _state.update { it.copy(printers = (it.printers + lan).distinctBy { p -> p.id }, lanAddress = "") }
    }

    fun selectPrinter(target: PrinterTarget) = viewModelScope.launch {
        settingsRepository.savePrinter(target.address)
        _state.update { it.copy(selected = target, message = "Printer saved") }
    }

    fun testPrint() = viewModelScope.launch {
        val target = state.value.selected ?: return@launch
        runCatching { printerEngine.printTest(target) }
            .onSuccess { _state.update { it.copy(message = "Test print queued") } }
            .onFailure { _state.update { it.copy(message = it.message ?: "Printer failed") } }
    }

    fun healthCheck() = viewModelScope.launch {
        val target = state.value.selected ?: return@launch
        val ok = printerEngine.health(target)
        _state.update { it.copy(message = if (ok) "Printer healthy" else "Printer offline") }
    }

    fun retryQueue() = viewModelScope.launch {
        printerEngine.retryQueued()
        _state.update { it.copy(message = "Retry queue processed") }
    }
}
