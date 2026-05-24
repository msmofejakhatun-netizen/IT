package com.restopro.captain.printer

import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import javax.inject.Inject

class EscPosFormatter @Inject constructor() {
    fun kot(order: OrderEntity, items: List<OrderItemEntity>): ByteArray = buildString {
        append("\u001B@")
        append("\u001Ba\u0001")
        append("RESTOPRO KOT\n")
        append("\u001Ba\u0000")
        append("Order: ${order.id.take(8)}\n")
        append("Captain: ${order.captainName}\n")
        append("--------------------------------\n")
        items.filterNot { it.isVoided }.forEach {
            append("${it.quantity} x ${it.name}\n")
            if (it.note.isNotBlank()) append("  Note: ${it.note}\n")
        }
        append("--------------------------------\n")
        append("\n\n\n")
        append("\u001DV\u0001")
    }.toByteArray(Charsets.UTF_8)

    fun test(): ByteArray = "RestoPro Captain\nPrinter OK\n\n\n".toByteArray(Charsets.UTF_8)
}
