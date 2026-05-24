package com.restopro.captain.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanServerDetector @Inject constructor() {
    suspend fun findServer(port: Int = 5000): String? = withContext(Dispatchers.IO) {
        val subnet = localSubnet() ?: return@withContext null
        coroutineScope {
            (1..254).chunked(24).forEach { chunk ->
                val result = chunk.map { host ->
                    async { probe("http://$subnet.$host:$port/") }
                }.awaitAll().firstOrNull { it != null }
                if (result != null) return@coroutineScope result
            }
            null
        }
    }

    private fun probe(baseUrl: String): String? = runCatching {
        val connection = URL("${baseUrl}api/health").openConnection() as HttpURLConnection
        connection.connectTimeout = 220
        connection.readTimeout = 220
        connection.requestMethod = "GET"
        if (connection.responseCode in 200..299) baseUrl else null
    }.getOrNull()

    private fun localSubnet(): String? {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        val address = interfaces.flatMap { Collections.list(it.inetAddresses) }
            .filterIsInstance<InetAddress>()
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.startsWith("192.168.") == true }
            ?.hostAddress ?: return null
        return address.substringBeforeLast(".")
    }
}
