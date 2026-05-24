package com.restopro.captain.socket

import com.google.gson.Gson
import com.restopro.captain.data.local.dao.TableDao
import com.restopro.captain.data.repository.toEntity
import com.restopro.captain.data.remote.dto.TableDto
import com.restopro.captain.utils.ServerConfigStore
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val serverConfigStore: ServerConfigStore,
    private val tableDao: TableDao,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    fun prepare() = Unit

    fun connect() {
        disconnect()
        val opts = IO.Options.builder()
            .setReconnection(true)
            .setReconnectionAttempts(Int.MAX_VALUE)
            .setReconnectionDelay(750)
            .setTimeout(3000)
            .build()
        socket = IO.socket(serverConfigStore.blockingBaseUrl(), opts).apply {
            on(Socket.EVENT_CONNECT) { _connected.value = true }
            on(Socket.EVENT_DISCONNECT) { _connected.value = false }
            on("table.updated") { args -> handleTableUpdate(args.firstOrNull()) }
            on("kot.ready") { /* Hook native notification channel here. */ }
            on("order.updated") { /* Order reconciliation is handled by periodic sync. */ }
            connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connected.value = false
    }

    fun emitBuffered(event: String, payload: JSONObject) {
        socket?.emit(event, payload)
    }

    private fun handleTableUpdate(raw: Any?) {
        val text = raw?.toString() ?: return
        scope.launch {
            runCatching {
                tableDao.upsertTable(gson.fromJson(text, TableDto::class.java).toEntity())
            }
        }
    }
}
