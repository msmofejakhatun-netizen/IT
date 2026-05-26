package com.restopro.captain.printer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanPrinterTransport @Inject constructor() : PrinterTransport {
    override val type: PrinterTransportType = PrinterTransportType.LAN

    override suspend fun discover(): List<PrinterTarget> = emptyList()

    override suspend fun print(target: PrinterTarget, payload: ByteArray) = withContext(Dispatchers.IO) {
        val host = target.address.substringBefore(':')
        val port = target.address.substringAfter(':', "9100").toInt()
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), 2000)
            socket.getOutputStream().use { out -> out.write(payload); out.flush() }
        }
    }

    override suspend fun health(target: PrinterTarget): Boolean = runCatching {
        val host = target.address.substringBefore(':')
        val port = target.address.substringAfter(':', "9100").toInt()
        Socket().use { it.connect(InetSocketAddress(host, port), 1200) }
        true
    }.getOrDefault(false)
}
