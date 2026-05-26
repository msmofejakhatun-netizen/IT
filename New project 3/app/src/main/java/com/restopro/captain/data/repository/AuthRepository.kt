package com.restopro.captain.data.repository

import com.restopro.captain.data.local.dao.SettingsDao
import com.restopro.captain.data.local.entity.CaptainEntity
import com.restopro.captain.data.local.entity.SettingsEntity
import com.restopro.captain.data.remote.api.AuthApi
import com.restopro.captain.data.remote.dto.LoginRequest
import com.restopro.captain.domain.model.LoginSession
import com.restopro.captain.domain.model.Role
import com.restopro.captain.utils.LanServerDetector
import com.restopro.captain.utils.SecureTextCipher
import com.restopro.captain.utils.ServerConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.system.measureTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val settingsDao: SettingsDao,
    private val serverConfigStore: ServerConfigStore,
    private val lanServerDetector: LanServerDetector,
    private val secureTextCipher: SecureTextCipher
) {
    suspend fun login(
        restaurantCode: String,
        username: String,
        password: String,
        serverIp: String,
        remember: Boolean
    ): LoginSession {
        val baseUrl = serverIp.toBaseUrl()
        serverConfigStore.saveServer(baseUrl)
        settingsDao.saveSettings(
            SettingsEntity(
                restaurantCode = restaurantCode,
                serverBaseUrl = baseUrl,
                rememberLogin = remember
            )
        )
        val response = authApi.login(LoginRequest(restaurantCode, username, password, serverIp))
        require(response.role.equals("CAPTAIN", ignoreCase = true)) { "Unauthorized captain role: ${response.role}" }
        serverConfigStore.saveToken(response.token)
        serverConfigStore.saveLoginHints(username, restaurantCode, serverIp)
        settingsDao.saveCaptain(
            CaptainEntity(
                id = response.captainId,
                restaurantId = response.restaurantId,
                restaurantName = response.restaurantName,
                name = response.captainName,
                username = response.username,
                role = response.role.uppercase(),
                token = secureTextCipher.encrypt(response.token),
                refreshToken = secureTextCipher.encrypt(response.refreshToken)
            )
        )
        return LoginSession(
            captainId = response.captainId,
            captainName = response.captainName,
            role = Role.valueOf(response.role.uppercase()),
            restaurantName = response.restaurantName,
            token = response.token
        )
    }

    suspend fun restoreOfflineSession(): LoginSession? {
        val settings = settingsDao.getSettings()
        if (settings?.rememberLogin != true) return null
        val captain = settingsDao.lastCaptain() ?: return null
        return LoginSession(
            captainId = captain.id,
            captainName = captain.name,
            role = Role.valueOf(captain.role.uppercase()),
            restaurantName = captain.restaurantName,
            token = secureTextCipher.decrypt(captain.token)
        )
    }

    suspend fun testConnection(serverIp: String): com.restopro.captain.data.remote.dto.PingResponse {
        serverConfigStore.saveServer(serverIp.toBaseUrl())
        val elapsed = measureTimeMillis { authApi.health() }
        val ok = runCatching { authApi.health().ok }.getOrDefault(false)
        val quality = when {
            !ok -> "FAILED"
            elapsed > 500 -> "SLOW"
            else -> "GOOD"
        }
        return com.restopro.captain.data.remote.dto.PingResponse(ok = ok, latencyMs = elapsed, quality = quality)
    }

    suspend fun autoDetectServer(): String? = lanServerDetector.findServer()

    suspend fun validateLanIp(serverIp: String): Boolean = withContext(Dispatchers.Default) {
        val host = serverIp.trim().removePrefix("http://").removePrefix("https://").substringBefore(":").substringBefore("/")
        if (!host.startsWith("192.168.")) return@withContext false
        try { InetAddress.getByName(host) != null } catch (_: UnknownHostException) { false }
    }

    private fun String.toBaseUrl(): String {
        val trimmed = trim()
        val withScheme = if (trimmed.startsWith("http")) trimmed else "http://$trimmed"
        val withPort = if (withScheme.substringAfter("://").contains(":")) withScheme else "$withScheme:5000"
        return if (withPort.endsWith("/")) withPort else "$withPort/"
    }
}
