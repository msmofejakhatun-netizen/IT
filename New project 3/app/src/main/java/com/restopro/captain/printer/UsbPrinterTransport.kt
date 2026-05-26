package com.restopro.captain.printer

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsbPrinterTransport @Inject constructor(
    @ApplicationContext private val context: Context
) : PrinterTransport {
    override val type: PrinterTransportType = PrinterTransportType.USB

    override suspend fun discover(): List<PrinterTarget> = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        manager.deviceList.values
            .filter { d -> d.interfaceCount > 0 && (0 until d.interfaceCount).any { d.getInterface(it).interfaceClass == UsbConstants.USB_CLASS_PRINTER } }
            .map { d -> PrinterTarget(id = "usb:${d.deviceId}", name = d.productName ?: "USB Printer", transport = type, address = d.deviceId.toString()) }
    }

    override suspend fun print(target: PrinterTarget, payload: ByteArray) {
        error("USB print pipeline requires runtime USB permission + endpoint negotiation. Hook vendor specific flow here.")
    }

    override suspend fun health(target: PrinterTarget): Boolean {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device: UsbDevice? = manager.deviceList.values.firstOrNull { it.deviceId.toString() == target.address }
        return device != null
    }
}
