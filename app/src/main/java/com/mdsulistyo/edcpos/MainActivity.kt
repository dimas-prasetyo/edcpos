package com.mdsulistyo.edcpos

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
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

        /*btnSend.setOnClickListener {
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
        }*/

        // Agak laen
        btnSend.setOnClickListener {
            checkData()
            sendCommandVersiLain()
        }

        // dengan metode usb serial for android
        btnOpen.setOnClickListener {
            //openDevice()
            openDeviceWithCustomProber()
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

    private fun openDeviceWithCustomProber() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Probe for our custom CDC devices, which use VID 0x1234
        // and PIDS 0x0001 and 0x0002.
        val customTable = ProbeTable()
        customTable.addProduct(0x1234, 0x0001, CdcAcmSerialDriver::class.java)
        customTable.addProduct(0x1234, 0x0002, CdcAcmSerialDriver::class.java)

        val prober = UsbSerialProber(customTable)

        // Find all available drivers from attached devices.
        val availableDrivers = prober.findAllDrivers(manager)
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

    private fun sendCommandVersiLain() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices: Map<String, UsbDevice> = manager.deviceList
        val mDevice: UsbDevice? = devices["/dev/bus/usb/001/002"]
        if (mDevice != null) {
            try {
                val connection: UsbDeviceConnection = manager.openDevice(mDevice)
                val c = mDevice.getInterface(0)
                val textView = findViewById<TextView>(R.id.tv_response)
                textView.text = "DEBUG sendCommandVersiLain : $c"
                val endpoint: UsbEndpoint? = mDevice.getInterface(0).getEndpoint(0)

                connection.claimInterface(mDevice.getInterface(0), true)
                //val msg = "0201500230323030303030303130303030303030303030303030303030303136383837303036323732303138393220202032353130303030303030303030303030303020204E30303030302020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020200308"
                val msg = "0201500130313030303030303030303130303030303030303030303030302020203136383837303036323732303138393232353130303030303030303020202020202020204E20202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020200318"
                //val msg = "0201500103"
                bytes = msg.decodeHex()
                Thread(Runnable {
                    // a potentially time consuming task
                    val result = connection.bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT)
                    textView.post {
                        Toast.makeText(this@MainActivity, "RESULT : $result", Toast.LENGTH_LONG).show()
                    }
                }).start()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkData() {
        val msg = "0201500130313030303030303030303130303030303030303030303030302020203136383837303036323732303138393232353130303030303030303020202020202020204E20202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020200318"
        val chunk = msg.chunked(2)
        Log.d("TAG", "checkData ${msg.length} ${chunk.size}")
        Log.d("TAG", "checkData chunk $chunk")
        Log.d("TAG", "checkData hexToByte ${msg.decodeHex()} ${msg.decodeHex().size}")
    }

    /*private fun hexToByte(data: String) : ByteArray {
        //remove any spaces from the string
        var msg = data
        msg = msg.replace(" ", "")
        val chunk = msg.chunked(2)
        //create a byte array the length of the
        //divided by 2 (Hex is 2 characters in length)
        val comBuffer = ByteArray(chunk.size)
        //loop through the length of the provided string
        for(i in chunk.indices) {
            //convert each set of 2 characters to a byte
            //and add to the array
           comBuffer[i] = chunk[i].toByte()
        }
        //return the array
        return comBuffer
    }*/

    /*private fun String.chunked(size: Int): List<String> {
        val nChunks = length / size
        return (0 until nChunks).map { substring(it * size, (it + 1) * size) }
    }*/

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}