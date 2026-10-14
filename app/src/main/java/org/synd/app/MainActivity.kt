package org.synd.app

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.synd.ble.SyndBleTransport

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var peers: TextView
    private lateinit var transport: SyndBleTransport
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 64, 36, 36)
            setBackgroundColor(Color.rgb(7, 10, 19))
        }
        fun label(value: String, size: Float): TextView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 28)
        }
        layout.addView(label("SYND  •  BLUETOOTH", 26f))
        status = label("Discovery build — messaging and voice not enabled", 16f)
        layout.addView(status)
        peers = label("Nearby devices: none", 17f)
        val start = Button(this).apply {
            text = "Find nearby Synd phones"
            setOnClickListener { requestAndStart() }
        }
        layout.addView(start)
        layout.addView(peers)
        layout.addView(label("Offline BLE discovery only. This build does not transmit messages or audio.", 14f))
        setContentView(layout)
        transport = SyndBleTransport(this)
        transport.onPeerFound = { id, name -> runOnUiThread {
            peers.append("\n$name ($id)")
        }}
        transport.onError = { message -> runOnUiThread { status.text = message } }
    }

    private fun requestAndStart() {
        val permissions = if (Build.VERSION.SDK_INT >= 31)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 100)
        } else startDiscovery()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            startDiscovery()
        else status.text = "Bluetooth permission required for discovery"
    }

    private fun startDiscovery() {
        if (started) return
        try {
            transport.startAdvertising(this)
            transport.startScan()
            started = true
            status.text = "Scanning and advertising nearby Synd devices"
        } catch (e: SecurityException) {
            status.text = "Bluetooth permission denied"
        } catch (e: Exception) {
            status.text = "Bluetooth error: ${e.message}"
        }
    }

    override fun onStop() {
        super.onStop()
        if (started) {
            transport.stopScan()
            transport.stopAdvertising()
            started = false
            status.text = "Discovery stopped"
        }
    }
}
