package com.restopro.captain.printer

enum class PrinterTransportType { BLUETOOTH, USB, LAN }
enum class PaperWidth { MM58, MM80 }

data class PrinterTarget(
    val id: String,
    val name: String,
    val transport: PrinterTransportType,
    val address: String,
    val kitchenStation: String = "MAIN",
    val paperWidth: PaperWidth = PaperWidth.MM80
)

data class PrintJob(
    val id: String,
    val target: PrinterTarget,
    val payload: ByteArray,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
