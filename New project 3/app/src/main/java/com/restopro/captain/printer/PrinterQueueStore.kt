package com.restopro.captain.printer

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.printerDataStore by preferencesDataStore("printer_queue_store")

@Singleton
class PrinterQueueStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val queueKey = stringPreferencesKey("print_queue")

    suspend fun load(): MutableList<QueuedPrintJob> {
        val raw = context.printerDataStore.data.first()[queueKey] ?: return mutableListOf()
        return runCatching {
            gson.fromJson(raw, object : TypeToken<MutableList<QueuedPrintJob>>() {}.type)
        }.getOrDefault(mutableListOf())
    }

    suspend fun save(queue: List<QueuedPrintJob>) {
        context.printerDataStore.edit { it[queueKey] = gson.toJson(queue) }
    }
}

data class QueuedPrintJob(
    val id: String,
    val target: PrinterTarget,
    val payloadBase64: String,
    val retryCount: Int,
    val createdAt: Long
)
