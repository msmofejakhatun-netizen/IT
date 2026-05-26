package com.restopro.captain.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val serverConfigStore: ServerConfigStore
) {
    suspend fun saveAuthToken(token: String) = serverConfigStore.saveToken(token)
    suspend fun saveLoginMeta(username: String, restaurantCode: String, serverIp: String) =
        serverConfigStore.saveLoginHints(username, restaurantCode, serverIp)

    fun authToken(): String? = serverConfigStore.blockingToken()
}
