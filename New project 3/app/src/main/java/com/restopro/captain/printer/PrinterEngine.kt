package com.restopro.captain.printer

import android.util.Base64
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterEngine @Inject constructor(
    private val formatter: EscPosFormatter,
    private val bt: BluetoothPrinterTransport,
    private val usb: UsbPrinterTransport,
    private val lan: LanPrinterTransport,
    private val queueStore: PrinterQueueStore
) {
    private fun transport(type: PrinterTransportType): PrinterTransport = when (type) {
        PrinterTransportType.BLUETOOTH -> bt
        PrinterTransportType.USB -> usb
        PrinterTransportType.LAN -> lan
    }

    suspend fun discoverAll(): List<PrinterTarget> = buildList {
        addAll(bt.discover())
        addAll(usb.discover())
    }

    suspend fun printKot(target: PrinterTarget, order: OrderEntity, items: List<OrderItemEntity>, includeQr: Boolean = true) {
        val payload = formatter.kot(order, items, target.paperWidth, includeQr)
        enqueueAndSend(target, payload)
    }

    suspend fun printTest(target: PrinterTarget) = enqueueAndSend(target, formatter.test(target.paperWidth))

    suspend fun health(target: PrinterTarget): Boolean = transport(target.transport).health(target)

    suspend fun retryQueued() = withContext(Dispatchers.IO) {
        val queue = queueStore.load()
        val remaining = mutableListOf<QueuedPrintJob>()
        queue.forEach { q ->
            val ok = runCatching {
                transport(q.target.transport).print(q.target, Base64.decode(q.payloadBase64, Base64.NO_WRAP))
            }.isSuccess
            if (!ok) remaining += q.copy(retryCount = q.retryCount + 1)
        }
        queueStore.save(remaining)
    }

    private suspend fun enqueueAndSend(target: PrinterTarget, payload: ByteArray) {
        val queued = QueuedPrintJob(
            id = UUID.randomUUID().toString(),
            target = target,
            payloadBase64 = Base64.encodeToString(payload, Base64.NO_WRAP),
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )
        val queue = queueStore.load()
        queue += queued
        queueStore.save(queue)
        retryQueued()
    }
}
