package com.restopro.captain.printer

import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import javax.inject.Inject

class EscPosFormatter @Inject constructor() {
    fun kot(order: OrderEntity, items: List<OrderItemEntity>, width: PaperWidth = PaperWidth.MM80, includeQr: Boolean = true): ByteArray = buildString {
        append("\u001B@")
        append("\u001Ba\u0001")
        append("RESTOPRO KOT\n")
        append("\u001Ba\u0000")
        append("Order: ${order.id.take(8)}\n")
        append("Captain: ${order.captainName}\n")
        append(line(width))
        items.filterNot { it.isVoided }.groupBy { it.name }.forEach { (name, grouped) ->
            append("${grouped.sumOf { it.quantity }} x $name\n")
            grouped.firstOrNull { it.note.isNotBlank() }?.let { append("  Note: ${it.note}\n") }
        }
        append(line(width))
        if (includeQr) {
            append("Scan: ORDER-${order.id.take(8)}\n")
        }
        append("\n\n\n")
        append("\u001DV\u0001")
    }.toByteArray(Charsets.UTF_8)

    fun test(width: PaperWidth = PaperWidth.MM80): ByteArray = buildString {
        append("RestoPro Captain\n")
        append("Printer OK (${width.name})\n")
        append("हिंदी தமிழ் عربى\n")
        append("\n\n")
    }.toByteArray(Charsets.UTF_8)

    private fun line(width: PaperWidth): String = if (width == PaperWidth.MM58) "--------------------------------\n" else "------------------------------------------\n"
}
