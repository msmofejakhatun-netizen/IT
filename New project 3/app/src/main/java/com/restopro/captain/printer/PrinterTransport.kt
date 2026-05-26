package com.restopro.captain.printer

interface PrinterTransport {
    val type: PrinterTransportType
    suspend fun discover(): List<PrinterTarget>
    suspend fun print(target: PrinterTarget, payload: ByteArray)
    suspend fun health(target: PrinterTarget): Boolean
}
