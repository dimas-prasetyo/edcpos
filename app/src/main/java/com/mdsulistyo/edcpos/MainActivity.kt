package com.mdsulistyo.edcpos

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : AppCompatActivity() {

    private lateinit var bytes: ByteArray
    private val TIMEOUT = 0
    private val forceClaim = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.tv_response)
        val btnLoad = findViewById<TextView>(R.id.btn_load)
        val btnSend = findViewById<TextView>(R.id.btn_send)
        val btnOpen = findViewById<TextView>(R.id.btn_open)

        var text = ""
        var d: UsbDevice? = null
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        btnLoad.setOnClickListener {
            // loading list device yang terkoneksi
            val deviceList = manager.deviceList
            text = "\nDevice count: ${deviceList.size}"
            for (device in deviceList) {
                // simpan value UsbDevice
                d = device.value
                text += "\nDevice: $d"
            }
            textView.text = text
        }

        btnSend.setOnClickListener {
            // get UsbInterface {A class representing an interface on a UsbDevice}
            d?.getInterface(0)?.also { intf ->
                // get UsbEndpoint {A class representing an endpoint on a UsbInterface}
                intf.getEndpoint(0)?.also { endpoint ->
                    // UsbDeviceConnection openDevice
                    manager.openDevice(d)?.apply {
                        val msg = ""
                        bytes = msg.toByteArray()
                        claimInterface(intf, forceClaim)
                        bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
                        Toast.makeText(this@MainActivity, "sent!!", Toast.LENGTH_LONG).show()
                    }
                    Toast.makeText(this@MainActivity, "not sent!!", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnOpen.setOnClickListener {
            openDevice()
        }
    }

    private fun openDevice() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        // Find all available drivers from attached devices.
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            val textView = findViewById<TextView>(R.id.tv_response)
            textView.text = "availableDrivers is empty"
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            Toast.makeText(this@MainActivity, "connection is nil", Toast.LENGTH_LONG).show()
            return;
        }

        val port = driver.ports[0] // Most devices have just one port (port 0)
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        val textView = findViewById<TextView>(R.id.tv_response)
        textView.text = "Connection is open = ${port.isOpen}"
    }
}