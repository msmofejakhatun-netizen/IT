package com.restopro.captain.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverDataStore by preferencesDataStore(Constants.DATASTORE_NAME)

@Singleton
class ServerConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTextCipher: SecureTextCipher
) {
    private val baseUrlKey = stringPreferencesKey("server_base_url")
    private val tokenKey = stringPreferencesKey("jwt_token")
    private val usernameKey = stringPreferencesKey("username")
    private val restaurantCodeKey = stringPreferencesKey("restaurant_code")
    private val serverIpKey = stringPreferencesKey("server_ip")

    val baseUrl: Flow<String> = context.serverDataStore.data.map {
        it[baseUrlKey].orEmpty().ifBlank { Constants.DEFAULT_BASE_URL }
    }

    fun blockingBaseUrl(): String = runBlocking {
        normalizeBaseUrl(baseUrl.first())
    }

    fun blockingToken(): String? = runBlocking {
        context.serverDataStore.data.first()[tokenKey]?.let { secureTextCipher.decrypt(it) }
    }

    suspend fun saveServer(baseUrl: String) {
        context.serverDataStore.edit { it[baseUrlKey] = normalizeBaseUrl(baseUrl) }
    }

    suspend fun saveToken(token: String) {
        context.serverDataStore.edit { it[tokenKey] = secureTextCipher.encrypt(token) }
    }

    suspend fun saveLoginHints(username: String, restaurantCode: String, serverIp: String) {
        context.serverDataStore.edit {
            it[usernameKey] = username
            it[restaurantCodeKey] = restaurantCode
            it[serverIpKey] = serverIp
        }
    }

    private fun normalizeBaseUrl(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return Constants.DEFAULT_BASE_URL
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
