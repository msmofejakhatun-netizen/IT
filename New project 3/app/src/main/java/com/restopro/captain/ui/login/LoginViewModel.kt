package com.restopro.captain.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restopro.captain.data.repository.AuthRepository
import com.restopro.captain.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val restaurantCode: String = "",
    val username: String = "",
    val password: String = "",
    val serverIp: String = "",
    val remember: Boolean = true,
    val loading: Boolean = false,
    val connectionOk: Boolean? = null,
    val latencyMs: Long? = null,
    val connectionQuality: String? = null,
    val snack: String? = null,
    val error: String? = null,
    val loggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    init {
        viewModelScope.launch {
            val session = authRepository.restoreOfflineSession()
            if (session != null) _state.update { it.copy(loggedIn = true) }
        }
    }

    fun update(transform: LoginUiState.() -> LoginUiState) {
        _state.update(transform)
    }

    fun detectServer() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        val server = authRepository.autoDetectServer()
        _state.update {
            it.copy(
                loading = false,
                serverIp = server ?: it.serverIp,
                error = if (server == null) "No local server found on this LAN" else null
            )
        }
    }

    fun testConnection() = viewModelScope.launch {
        val server = state.value.serverIp
        _state.update { it.copy(loading = true, error = null, connectionOk = null) }
        runCatching { authRepository.testConnection(server) }
            .onSuccess { ping ->
                val status = when {
                    !ping.ok -> "❌ Failed"
                    ping.quality == "SLOW" -> "⚠ Slow Connection"
                    else -> "✅ Connected"
                }
                _state.update { it.copy(loading = false, connectionOk = ping.ok, latencyMs = ping.latencyMs, connectionQuality = ping.quality, snack = "$status (${ping.latencyMs} ms)", error = if (ping.ok) null else "Server unreachable") }
            }
            .onFailure { _state.update { it.copy(loading = false, connectionOk = false, error = "LAN disconnected or server offline") } }
    }

    fun consumeSnack() { _state.update { it.copy(snack = null) } }

    fun login() = viewModelScope.launch {
        val s = state.value
        if (s.restaurantCode.isBlank() || s.username.isBlank() || s.password.isBlank() || s.serverIp.isBlank()) {
            _state.update { it.copy(error = "All fields are required") }
            return@launch
        }
        if (s.password.length < 4) {
            _state.update { it.copy(error = "Password too short") }
            return@launch
        }
        if (!authRepository.validateLanIp(s.serverIp)) {
            _state.update { it.copy(error = "Invalid LAN IP. Use 192.168.x.x") }
            return@launch
        }
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            loginUseCase(s.restaurantCode, s.username, s.password, s.serverIp, s.remember)
        }.onSuccess {
            _state.update { it.copy(loading = false, loggedIn = true) }
        }.onFailure { error ->
            val offline = authRepository.restoreOfflineSession()
            _state.update {
                it.copy(
                    loading = false,
                    loggedIn = offline != null,
                    error = if (offline == null) error.message ?: "Invalid credentials / server unreachable" else null
                )
            }
        }
    }
}
