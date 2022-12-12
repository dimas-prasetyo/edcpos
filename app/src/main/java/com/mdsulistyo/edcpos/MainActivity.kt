package com.mdsulistyo.edcpos

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.mdsulistyo.edcpos.EDCManager.SendRequestToEDCBCA
import com.mdsulistyo.edcpos.databinding.ActivityMainBinding
import kotlin.experimental.xor
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bytes: ByteArray
    private var bytesResponse = byteArrayOf()
    private lateinit var usedPort: UsbSerialPort
    private lateinit var mDevice: UsbDevice
    private lateinit var mUsbManger: UsbManager
    private val TIMEOUT = 0
    private val forceClaim = true
    private val ACTION_USB_PERMISSION = "permission"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val textView = findViewById<TextView>(R.id.tv_response)
        val btnLoad = findViewById<TextView>(R.id.btn_load)
        val btnSend = findViewById<TextView>(R.id.btn_send)
        val btnOpen = findViewById<TextView>(R.id.btn_open)*/

        mUsbManger = getSystemService(Context.USB_SERVICE) as UsbManager
        /*val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(broadcastReceiver, filter)*/
        val deviceList = mUsbManger.deviceList
        if (deviceList.size > 0){
            mDevice = deviceList.values.elementAt(0)
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            mUsbManger.requestPermission(mDevice, permissionIntent)
        }

        setupClickListener()

        btnLoad.setOnClickListener {
            // loading list device yang terkoneksi

            text = "\nDevice count: ${deviceList.size}"
            for (device in deviceList) {
                // simpan value UsbDevice
                usbDevice = device.value
                text += "\nDevice: $usbDevice"
            }
            textView.text = text


        }

        btnSend.setOnClickListener {
            /*usedPort.read(bytesResponse)
            if (bytesResponse.isNotEmpty()){
                println("RESPON: " + bytesResponse)
            } else{
                println("RESPON KOSONG")
            }*/

            /*val devices: Map<String, UsbDevice> = manager.deviceList
            val mDevice: UsbDevice = devices.values.elementAt(0)
            // get UsbInterface {A class representing an interface on a UsbDevice}
            val interfaceList = mDevice.interfaceCount
            mDevice.getInterface(interfaceList - 1).also { intf ->
                // get UsbEndpoint {A class representing an endpoint on a UsbInterface}
                val endpointList = intf.endpointCount
                intf.getEndpoint(endpointList - 1).also { endpoint ->
                    // UsbDeviceConnection openDevice
                    try {
                        manager.openDevice(mDevice).apply {
                            val msg = SendSaleRequestToEDCBCA(1.00)
                            bytes = msg.decodeHex()
                            claimInterface(intf, forceClaim)
                            Thread(Runnable {
                                // a potentially time consuming task
                                val result = bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
                                //println("RESULT: " + result)
                                textView.post {
                                    Toast.makeText(this@MainActivity, "RESULT : $result", Toast.LENGTH_LONG).show()
                                }


                            }).start()
                        }
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, "Gagal: " + e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }*/
        }

        // Agak laen
        /*btnSend.setOnClickListener {
            checkData()
            //sendCommandVersiLain()
            //sendTest()
        }*/

        // dengan metode usb serial for android
    }

    private fun setupClickListener() {
        binding.btnLoad.setOnClickListener {
            loadConnectedDevice()
        }

        binding.btnSend.setOnClickListener{
            sendUsingBulkTransfer()
        }

        binding.btnOpen.setOnClickListener {
            openDeviceWithCustomProber()
        }

        binding.btnReceive.setOnClickListener {
            receiveResponse()
        }
    }

    private fun loadConnectedDevice() {
        var text = ""
        val deviceList = mUsbManger.deviceList
        text = "\nDevice count: ${deviceList.size}"
        for (device in deviceList) {
            // simpan value UsbDevice
            val usbDevice = device.value
            text += "\nDevice: $usbDevice"
        }
        binding.tvResponse.text = text
    }

    private fun receiveResponse() {
        val buffer = ByteArray(256)
        val len = usedPort.read(buffer, TIMEOUT)
        val strResponse = String(buffer)
        binding.tvResponse.text = strResponse
    }

    private fun sendUsingBulkTransfer() {
        val interfaceList = mDevice.interfaceCount
        mDevice.getInterface(interfaceList - 1).also { intf ->
            val endpointList = intf.endpointCount
            intf.getEndpoint(endpointList - 1).also { endpoint ->
                try {
                    mUsbManger.openDevice(mDevice).apply {
                        val msg = SendSaleRequestToEDCBCA(1.00)
                        bytes = msg.decodeHex()
                        claimInterface(intf, forceClaim)
                        Thread(Runnable {
                            val result = bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread

                        }).start()
                    }
                } catch (e: Exception){
                    Toast.makeText(this@MainActivity, "Gagal: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openDevice() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList

        val mDevice: UsbDevice = deviceList.values.elementAt(0)


        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        manager.requestPermission(mDevice, permissionIntent)

        //val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        // Find all available drivers from attached devices.
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        println("DRIVER SIZE: " + availableDrivers.size)
        if (availableDrivers.isEmpty()) {
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
    }

    private fun openDeviceWithCustomProber() {
        // Probe for our custom CDC devices, which use VID 0x1234
        // and PIDS 0x0001 and 0x0002.
        val customTable = ProbeTable()
        customTable.addProduct(mDevice.vendorId, mDevice.productId, CdcAcmSerialDriver::class.java)

        val prober = UsbSerialProber(customTable)

        // Find all available drivers from attached devices.
        val availableDrivers = prober.findAllDrivers(mUsbManger)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = mUsbManger.openDevice(driver.device)
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            Toast.makeText(this@MainActivity, "connection is nil", Toast.LENGTH_LONG).show()
            return;
        }

        usedPort = driver.ports[0] // Most devices have just one port (port 0)
        usedPort.open(connection)
        usedPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        val msg = SendSaleRequestToEDCBCA(5275.00)
        bytes = msg.decodeHex()

        Thread(Runnable {
            val result = usedPort.write(bytes, TIMEOUT)
        }).start()
    }

    private fun sendCommandVersiLain() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices: Map<String, UsbDevice> = manager.deviceList
        val mDevice: UsbDevice = devices.values.elementAt(0)
        if (mDevice != null) {
            try {
                val connection: UsbDeviceConnection = manager.openDevice(mDevice)
                val c = mDevice.getInterface(0)
                val endpoint: UsbEndpoint? = mDevice.getInterface(0).getEndpoint(0)

                connection.claimInterface(mDevice.getInterface(0), true)

                val tesMsg = ""
                bytes = tesMsg.decodeHex()
                Thread(Runnable {
                    val result = connection.bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT)
                }).start()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "GAGAL" + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkData() {
        val msg = "0201500130313030303030303030303130303030303030303030303030302020203136383837303036323732303138393232353130303030303030303020202020202020204E20202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020200318"
        val chunk = msg.chunked(2)
        /*Log.d("TAG", "checkData ${msg.length} ${chunk.size}")
        Log.d("TAG", "checkData chunk $chunk")
        Log.d("TAG", "checkData hexToByte ${msg.decodeHex()} ${msg.decodeHex().size}")*/

        val tesMsg = "01000000000100000000000000   1688700627201892251000000000        N                                                                                   "
        //val tesMsg = "02015001303130303030303030303031303030303030303030303"
        val builder: StringBuilder = StringBuilder(msg.length * 2)

        val ch: CharArray = tesMsg.toCharArray()
        for (i in ch.indices) {
            val hexString = Integer.toHexString(ch[i].code)
            builder.append(hexString)
        }

        val msg_frame = StringBuilder()
        msg_frame.append("02" + "0150" + "01" + builder.toString() + "03")
        val lrcs = msg_frame.toString().decodeHex()

        var lrc = lrcs[0]
        for (i in 1 until lrcs.size - 1) {
            lrc = lrc xor lrcs[i + 1]
        }
        val lrcCh: CharArray = lrc.toString().toCharArray()
        for (i in lrcCh.indices) {
            val hexString = Integer.toHexString(lrcCh[i].code)
            msg_frame.append(hexString)
        }

        bytes = msg.decodeHex()

    }

    fun SendSaleRequestToEDCBCA(paymentAmount: Double): String {
        val requestmsg = EDCManager.EDCBCARequestMessage()
        requestmsg._TransType.value = "01"
        requestmsg._TransAmount.value = paymentAmount.roundToInt().toString() + "00"
        requestmsg._PAN.value = "1688700627201892"
        requestmsg._ExpiryDate.value = "2510"
        return SendRequestToEDCBCA(requestmsg).uppercase()
    }

    fun SendSaleOtherAmountRequestToEDCBCA(paymentAmount: Double, otherAmount: Double): String {
        val requestmsg = EDCManager.EDCBCARequestMessage()
        requestmsg._TransType.value = "02"
        requestmsg._TransAmount.value = paymentAmount.roundToInt().toString() + "00"
        requestmsg._OtherAmount.value = otherAmount.roundToInt().toString() + "00"
        requestmsg._PAN.value = "1688700627201892"
        requestmsg._ExpiryDate.value = "2510"
        return SendRequestToEDCBCA(requestmsg)
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

}