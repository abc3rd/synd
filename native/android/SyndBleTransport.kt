package org.synd.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import java.util.UUID

/**
 * Foreground BLE discovery/advertising foundation.
 * Caller must request BLUETOOTH_SCAN, BLUETOOTH_CONNECT and BLUETOOTH_ADVERTISE on API 31+.
 * No user data is exchanged before peer authentication is implemented.
 */
@SuppressLint("MissingPermission")
class SyndBleTransport(context: Context) {
    companion object {
        val SERVICE_ID: UUID = UUID.fromString("7c31a4e0-8d62-4f1d-a9b3-104c8a63d001")
        val RX_ID: UUID = UUID.fromString("7c31a4e0-8d62-4f1d-a9b3-104c8a63d002")
        val TX_ID: UUID = UUID.fromString("7c31a4e0-8d62-4f1d-a9b3-104c8a63d003")
    }
    private val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter get() = manager.adapter
    private var gattServer: BluetoothGattServer? = null
    private val peers = mutableMapOf<String, BluetoothDevice>()
    var onPeerFound: ((String, String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            peers[device.address] = device
            onPeerFound?.invoke(device.address, device.name ?: "Nearby Synd device")
        }
        override fun onScanFailed(errorCode: Int) {
            onError?.invoke("BLE scan failed: $errorCode")
        }
    }
    private val serverCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_WRITE_NOT_PERMITTED, offset, null)
            }
        }
    }
    fun startScan() {
        val scanner = adapter.bluetoothLeScanner ?: run {
            onError?.invoke("Bluetooth scanner unavailable")
            return
        }
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_ID)).build())
        scanner.startScan(filters, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
    }
    fun stopScan() { adapter.bluetoothLeScanner?.stopScan(scanCallback) }

    fun startAdvertising(context: Context) {
        val advertiser = adapter.bluetoothLeAdvertiser ?: run {
            onError?.invoke("Bluetooth advertising unsupported")
            return
        }
        val service = BluetoothGattService(SERVICE_ID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        service.addCharacteristic(BluetoothGattCharacteristic(
            RX_ID, BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE))
        service.addCharacteristic(BluetoothGattCharacteristic(
            TX_ID, BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ))
        gattServer = manager.openGattServer(context, serverCallback)
        gattServer?.addService(service)
        val settings = AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true).build()
        val data = AdvertiseData.Builder().addServiceUuid(ParcelUuid(SERVICE_ID))
            .setIncludeDeviceName(false).build()
        advertiser.startAdvertising(settings, data, advertiseCallback)
    }
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            onError?.invoke("BLE advertising failed: $errorCode")
        }
    }
    fun stopAdvertising() {
        adapter.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
    }
}
