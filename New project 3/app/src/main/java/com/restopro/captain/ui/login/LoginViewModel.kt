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
        val ok = authRepository.testConnection(server)
        _state.update { it.copy(loading = false, connectionOk = ok, error = if (ok) null else "Server not reachable") }
    }

    fun login() = viewModelScope.launch {
        val s = state.value
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
                    error = if (offline == null) error.message ?: "Login failed" else null
                )
            }
        }
    }
}
