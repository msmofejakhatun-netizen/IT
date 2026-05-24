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
        val response = authApi.login(LoginRequest(restaurantCode, username, password))
        serverConfigStore.saveToken(response.token)
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

    suspend fun testConnection(serverIp: String): Boolean {
        serverConfigStore.saveServer(serverIp.toBaseUrl())
        return runCatching { authApi.health().ok }.getOrDefault(false)
    }

    suspend fun autoDetectServer(): String? = lanServerDetector.findServer()

    private fun String.toBaseUrl(): String {
        val trimmed = trim()
        val withScheme = if (trimmed.startsWith("http")) trimmed else "http://$trimmed"
        val withPort = if (withScheme.substringAfter("://").contains(":")) withScheme else "$withScheme:5000"
        return if (withPort.endsWith("/")) withPort else "$withPort/"
    }
}
