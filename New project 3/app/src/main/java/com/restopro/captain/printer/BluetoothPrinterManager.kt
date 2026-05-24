package com.restopro.captain.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.restopro.captain.data.local.entity.OrderEntity
import com.restopro.captain.data.local.entity.OrderItemEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val formatter: EscPosFormatter
) {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    @SuppressLint("MissingPermission")
    fun pairedPrinters(): List<BluetoothPrinter> {
        if (!hasBluetoothPermission()) return emptyList()
        return adapter?.bondedDevices.orEmpty()
            .map { BluetoothPrinter(it.name ?: "Thermal Printer", it.address) }
            .sortedBy { it.name }
    }

    suspend fun printKot(address: String, order: OrderEntity, items: List<OrderItemEntity>) {
        write(address, formatter.kot(order, items))
    }

    suspend fun testPrint(address: String) {
        write(address, formatter.test())
    }

    @SuppressLint("MissingPermission")
    private suspend fun write(address: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        require(hasBluetoothPermission()) { "Bluetooth permission is required" }
        val device: BluetoothDevice = adapter?.getRemoteDevice(address)
            ?: error("Bluetooth printer not available")
        adapter?.cancelDiscovery()
        device.createRfcommSocketToServiceRecord(sppUuid).use { socket ->
            socket.connect()
            socket.outputStream.use { stream ->
                stream.write(bytes)
                stream.flush()
            }
        }
    }

    private fun hasBluetoothPermission(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
}

data class BluetoothPrinter(val name: String, val address: String)
