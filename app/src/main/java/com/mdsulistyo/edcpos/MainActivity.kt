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
import com.mdsulistyo.edcpos.EDCManager.GetErrorMesssage
import com.mdsulistyo.edcpos.EDCManager.ReceiveResult
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

        mUsbManger = getSystemService(Context.USB_SERVICE) as UsbManager


        setupClickListener()
        //binding.btnPermission.callOnClick()

    }

    private fun setupClickListener() {
        binding.btnPermission.setOnClickListener {
            val deviceList = mUsbManger.deviceList
            if (deviceList.size > 0){
                mDevice = deviceList.values.elementAt(0)
            }
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            mUsbManger.requestPermission(mDevice, permissionIntent)
        }

        binding.btnLoad.setOnClickListener {
            loadConnectedDevice()
        }

        /*binding.btnSend.setOnClickListener{
            //sendUsingBulkTransfer()
            val msg = SendSaleRequestToEDCBCA(1.00)
            bytes = msg.decodeHex()

            Thread(Runnable {
                usedPort.write(bytes, TIMEOUT)
            }).start()
        }*/

        binding.btnOpen.setOnClickListener {
            openDeviceWithCustomProber()
        }

        binding.btnSettlement.setOnClickListener {
            val msg = SendSattlementToEDCBCA()
            bytes = msg.decodeHex()

            Thread(Runnable {
                usedPort.write(bytes, TIMEOUT)
            }).start()
        }

        binding.btnReceive.setOnClickListener {
            receiveResponse()
        }

        /*binding.btnDisconnect.setOnClickListener {
            if (usedPort.isOpen){
                usedPort.close()
            }
        }*/
    }

    private fun loadConnectedDevice() {
        var text = ""
        val deviceList = mUsbManger.deviceList
        text = "\nDevice count: ${deviceList.size}"
        for (device in deviceList) {
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

        var responseString = ""
        for (i in strResponse){
            if (i in 'a'..'z' || i in 'A'..'Z' || i in '0'..'9' || i == ' '){
                responseString += i
            }
        }


        val responseMsg = EDCManager.EDCBCAResponseMessage()
        if (responseString.length == 199){
            ReceiveResult(responseString, responseMsg)
            Toast.makeText(this, GetErrorMesssage(responseMsg.RespCode()), Toast.LENGTH_LONG).show()
        }
    }

    private fun sendUsingBulkTransfer() {
        val interfaceList = mDevice.interfaceCount
        mDevice.getInterface(interfaceList - 1).also { intf ->
            val endpointList = intf.endpointCount
            intf.getEndpoint(endpointList - 1).also { endpoint ->
                try {
                    mUsbManger.openDevice(mDevice).apply {
                        val msg = SendSaleOtherAmountRequestToEDCBCA(10000.00, 2000.00)
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


    private fun openDeviceWithCustomProber() {
        val customTable = ProbeTable()
        customTable.addProduct(mDevice.vendorId, mDevice.productId, CdcAcmSerialDriver::class.java)

        val prober = UsbSerialProber(customTable)

        // Find all available drivers from attached devices.
        val availableDrivers = prober.findAllDrivers(mUsbManger)
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this@MainActivity, "driver available is empty", Toast.LENGTH_LONG).show()
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = mUsbManger.openDevice(driver.device)
        if (connection == null) {
            Toast.makeText(this@MainActivity, "connection is nil", Toast.LENGTH_LONG).show()
            return;
        }

        usedPort = driver.ports[0] // Most devices have just one port (port 0)
        usedPort.open(connection)
        usedPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        /*val msg = SendVoidSaleToEDCBCA()
        bytes = msg.decodeHex()

        Thread(Runnable {
            usedPort.write(bytes, TIMEOUT)
        }).start()*/
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
        return SendRequestToEDCBCA(requestmsg).uppercase()
    }

    fun SendSattlementToEDCBCA(): String {
        val requestmsg = EDCManager.EDCBCARequestMessage()
        requestmsg._TransType.value = "10"
        requestmsg._DCCFlag.value = "N"
        return SendRequestToEDCBCA(requestmsg).uppercase()
    }

    fun SendVoidSaleToEDCBCA(): String {
        val requestmsg = EDCManager.EDCBCARequestMessage()
        requestmsg._TransType.value = "08"
        requestmsg._CancelReason.value = "02"
        requestmsg._InvoiceNumber.value = "100009"
        return SendRequestToEDCBCA(requestmsg).uppercase()
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

}