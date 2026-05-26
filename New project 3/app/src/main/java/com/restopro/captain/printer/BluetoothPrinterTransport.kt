package com.restopro.captain.printer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothPrinterTransport @Inject constructor(
    private val bluetooth: BluetoothPrinterManager
) : PrinterTransport {
    override val type: PrinterTransportType = PrinterTransportType.BLUETOOTH

    override suspend fun discover(): List<PrinterTarget> = bluetooth.pairedPrinters().map {
        PrinterTarget(id = "bt:${it.address}", name = it.name, transport = type, address = it.address)
    }

    override suspend fun print(target: PrinterTarget, payload: ByteArray) {
        bluetooth.printRaw(target.address, payload)
    }

    override suspend fun health(target: PrinterTarget): Boolean = bluetooth.healthCheck(target.address)
}
