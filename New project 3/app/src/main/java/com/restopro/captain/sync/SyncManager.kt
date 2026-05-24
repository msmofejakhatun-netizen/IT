package com.restopro.captain.sync

import com.google.gson.Gson
import com.restopro.captain.data.local.dao.OrderDao
import com.restopro.captain.data.local.dao.SyncDao
import com.restopro.captain.data.remote.api.SyncApi
import com.restopro.captain.data.remote.dto.SyncEnvelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class SyncManager @Inject constructor(
    private val syncDao: SyncDao,
    private val orderDao: OrderDao,
    private val syncApi: SyncApi,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            while (isActive) {
                flushQueue()
                delay(10_000)
            }
        }
    }

    suspend fun flushQueue() {
        syncDao.due().forEach { item ->
            val result = runCatching {
                syncApi.sync(SyncEnvelope(item.operation, item.entityType, item.payloadJson))
            }
            result.onSuccess { sync ->
                if (sync.accepted) {
                    syncDao.remove(item.id)
                    if (item.entityType == "ORDER") orderDao.markSynced(item.entityId, sync.serverId)
                } else {
                    syncDao.markFailed(item.id, sync.message ?: "Rejected by server", nextAttempt(item.retryCount))
                }
            }.onFailure {
                syncDao.markFailed(item.id, it.message ?: "Sync failed", nextAttempt(item.retryCount))
            }
        }
    }

    private fun nextAttempt(retryCount: Int): Long {
        val delayMs = min(60_000L, 2_000L * (retryCount + 1))
        return System.currentTimeMillis() + delayMs
    }
}
