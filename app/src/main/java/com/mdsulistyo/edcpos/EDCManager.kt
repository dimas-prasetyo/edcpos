package com.mdsulistyo.edcpos

import kotlin.experimental.xor
import kotlin.math.roundToInt


object EDCManager {

    fun GetErrorMesssage(responseCode: String): String {
        when (responseCode) {
            "54" -> return "Decline Expired Card"
            "55" -> return "Decline Incorrect PIN"
            "P2" -> return "Read Card Error"
            "P3" -> return "User press Cancel on EDC"
            "Z3" -> return "EMV Card Decline"
            "CE" -> return "Connection Error/Line Busy"
            "TO" -> return "Connection Time Out"
            "PT" -> return "EDC Problem"
            "PS" -> return "Settlement Failed"
            "aa" -> return "Decline (aa represent two digit alphanumeric value from EDC)"
        }
        return ""
    }

    fun SendFlazzSaleRequestToEDCBCA(paymentAmount: Double): String {
        val requestmsg = EDCBCARequestMessage()
        requestmsg._TransType.value = "06" // Flazz
        requestmsg._TransAmount.value = paymentAmount.roundToInt().toString() + "00"
        return SendRequestToEDCBCA(requestmsg)
    }

    fun SendVoidRequestToEDCBCA(paymentRef: String): String {
        val requestmsg= EDCBCARequestMessage()
        requestmsg._TransType.value = "08" //void sale
        requestmsg._CancelReason.value = "02"
        requestmsg._InvoiceNumber.value = paymentRef
        return SendRequestToEDCBCA(requestmsg)
    }

    fun SendSettlementRequestToEDCBCA(): String {
        val requestmsg = EDCBCARequestMessage()
        requestmsg._TransType.value = "10"; //settlement
        requestmsg._InstallmentFlag.value = "Y";
        requestmsg._RedeemFlag.value = "Y";
        requestmsg._DCCFlag.value = "N";
        requestmsg._InstallmentPlan.value = "001";
        requestmsg._InstallmentTenor.value = "06";

        return SendRequestToEDCBCA(requestmsg)
    }

    fun ReceiveResult(hexstring: String) {
        val respData = EDCBCAResponseMessage()
        var pos = 0
        respData._TransType.value = hexstring.substring(pos, respData._TransType.length)
        pos += respData._TransType.length
        respData._TransAmount.value = hexstring.substring(pos, respData._TransAmount.length)
        pos += respData._TransAmount.length
        respData._OtherAmount.value = hexstring.substring(pos, respData._OtherAmount.length)
        pos += respData._OtherAmount.length
        respData._PAN.value = hexstring.substring(pos, respData._PAN.length)
        pos += respData._PAN.length
        respData._ExpiryDate.value = hexstring.substring(pos, respData._ExpiryDate.length)
        pos += respData._ExpiryDate.length
        respData._RespCode.value = hexstring.substring(pos, respData._RespCode.length)
        pos += respData._RespCode.length
        respData._RRN.value = hexstring.substring(pos, respData._RRN.length)
        pos += respData._RRN.length
        respData._ApprovalCode.value = hexstring.substring(pos, respData._ApprovalCode.length)
        pos += respData._ApprovalCode.length
        respData._Date.value = hexstring.substring(pos, respData._Date.length)
        pos += respData._Date.length
        respData._Time.value = hexstring.substring(pos, respData._Time.length)
        pos += respData._Time.length
        respData._MerchaintId.value = hexstring.substring(pos, respData._MerchaintId.length)
        pos += respData._MerchaintId.length
        respData._TerminalId.value = hexstring.substring(pos, respData._TerminalId.length)
        pos += respData._TerminalId.length
        respData._OfflineFlag.value = hexstring.substring(pos, respData._OfflineFlag.length)
        pos += respData._OfflineFlag.length
        respData._CardholderName.value = hexstring.substring(pos, respData._CardholderName.length)
        pos += respData._CardholderName.length
        respData._PANCashierCard.value = hexstring.substring(pos, respData._PANCashierCard.length)
        pos += respData._PANCashierCard.length
        respData._InvoiceNumber.value = hexstring.substring(pos, respData._InvoiceNumber.length)
        pos += respData._InvoiceNumber.length
        respData._BatchNumber.value = hexstring.substring(pos, respData._BatchNumber.length)
        pos += respData._BatchNumber.length
        respData._IssuerId.value = hexstring.substring(pos, respData._IssuerId.length)
        pos += respData._IssuerId.length
        respData._InstallmentFlag.value = hexstring.substring(pos, respData._InstallmentFlag.length)
        pos += respData._InstallmentFlag.length
        respData._DCCFlag.value = hexstring.substring(pos, respData._DCCFlag.length)
        pos += respData._DCCFlag.length
        respData._RedeemFlag.value = hexstring.substring(pos, respData._RedeemFlag.length)
        pos += respData._RedeemFlag.length
        respData._InformationAmount.value = hexstring.substring(pos, respData._InformationAmount.length)
        pos += respData._InformationAmount.length
        respData._DCCDecimalPlace.value = hexstring.substring(pos, respData._DCCDecimalPlace.length)
        pos += respData._DCCDecimalPlace.length
        respData._DCCCurrencyName.value = hexstring.substring(pos, respData._DCCCurrencyName.length)
        pos += respData._DCCCurrencyName.length
        respData._DCCExchangeRate.value = hexstring.substring(pos, respData._DCCExchangeRate.length)
        pos += respData._DCCExchangeRate.length
        respData._CouponFlag.value = hexstring.substring(pos, respData._CouponFlag.length)
        pos += respData._CouponFlag.length
        respData._Filler.value = hexstring.substring(pos, respData._Filler.length)
        pos += respData._Filler.length
    }

    fun SendRequestToEDCBCA(requestmsg: EDCBCARequestMessage): String {
        try {
            val msg_frame = StringBuilder()
            val msg_data = StringBuilder()
            msg_data.append(requestmsg.TransType()); //trans type
            msg_data.append(requestmsg.TransAmount()); //trans amount
            msg_data.append(requestmsg.OtherAmount()); // other amount
            msg_data.append(requestmsg.PAN()); // PAN
            msg_data.append(requestmsg.ExpiryDate()); // Expire date
            msg_data.append(requestmsg.CancelReason()); // cancel reason
            msg_data.append(requestmsg.InvoiceNumber()); // invoice number
            msg_data.append(requestmsg.AuthorIDResponse()); // author ID respon
            msg_data.append(requestmsg.InstallmentFlag()); // installment Flag
            msg_data.append(requestmsg.RedeemFlag()); // redeem indicator
            msg_data.append(requestmsg.DCCFlag()); // DCC flag
            msg_data.append(requestmsg.InstallmentPlan()); // installment plan
            msg_data.append(requestmsg.InstallmentTenor()); // installment tenor
            msg_data.append(requestmsg.GenericData());
            msg_data.append(requestmsg.Filler("", 149 - msg_data.toString().length))

            val builder: StringBuilder = StringBuilder(msg_data.length * 2)

            val ch: CharArray = msg_data.toString().toCharArray()
            for (i in ch.indices) {
                val hexString = Integer.toHexString(ch[i].code)
                builder.append(hexString)
            }

            msg_frame.append("02" + "0150" + "01" + builder.toString() + "03")

            val lrcs = msg_frame.toString().decodeHex()

            var lrc = lrcs[0]
            for (i in 0 until lrcs.size) {
                lrc = lrc xor lrcs[i]
            }
            val hexLrc = Integer.toHexString(lrc.toInt())

            msg_frame.append(hexLrc)

            return msg_frame.toString()

        } catch (e: Exception){
            println("error: " + e.message)
            return "error: " + e.message
        }
    }


    class EDCBCARequestMessage {
        val _TransType = dataString("0", 2)
        val _TransAmount = dataString("0", 12)
        val _OtherAmount = dataString("0", 12)
        val _PAN = dataString(" ", 19)
        val _ExpiryDate = dataString("0", 4)
        val _CancelReason = dataString("0", 2)
        val _InvoiceNumber = dataString("0", 6)
        val _AuthorIDResponse = dataString(" ", 6) //Auth code
        val _InstallmentFlag = dataString(" ", 1)
        val _RedeemFlag = dataString(" ", 1)
        val _DCCFlag = dataString("N", 1)
        val _InstallmentPlan = dataString(" ", 3)
        val _InstallmentTenor = dataString(" ", 2)
        val _GenericData = dataString(" ", 12)
        val _ReffNumber = dataString(" ", 12)
        val _Filler: dataString =  dataString("", 0)

        fun c_InvoiceNumber(data: String): String {
            return if (data.length > 6) data.substring(0, 6) else data
        }

        fun TransType(): String{
            return _TransType.value
        }

        fun TransAmount(): String{
            if (_TransAmount.value.length < _TransAmount.length){
                _TransAmount.value = _TransAmount.value.paddingLeft(_TransAmount.length, "0")
            }
            return _TransAmount.value
        }

        fun OtherAmount(): String{
            if (_OtherAmount.value.length < _OtherAmount.length){
                _OtherAmount.value = _OtherAmount.value.paddingLeft(_OtherAmount.length, "0")
            }
            return _OtherAmount.value
        }

        fun PAN(): String{
            if (_PAN.value.length < _PAN.length){
                _PAN.value = _PAN.value.paddingLeft(_PAN.length, " ")
            }
            return _PAN.value
        }

        fun ExpiryDate(): String{
            if (_ExpiryDate.value.length < _ExpiryDate.length){
                _ExpiryDate.value = _ExpiryDate.value.paddingLeft(_ExpiryDate.length, "0")
            }
            return _ExpiryDate.value
        }


        fun CancelReason(): String{
            if (_CancelReason.value.length < _CancelReason.length){
                _CancelReason.value = _CancelReason.value.paddingLeft(_CancelReason.length, "0")
            }
            return _CancelReason.value
        }

        fun InvoiceNumber(): String{
            if (_InvoiceNumber.value.length < _InvoiceNumber.length){
                _InvoiceNumber.value = _InvoiceNumber.value.paddingLeft(_InvoiceNumber.length, "0")
            }
            return _InvoiceNumber.value
        }

        fun AuthorIDResponse(): String{
            if (_AuthorIDResponse.value.length < _AuthorIDResponse.length){
                _AuthorIDResponse.value = _AuthorIDResponse.value.paddingLeft(_AuthorIDResponse.length, " ")
            }
            return _AuthorIDResponse.value
        }

        fun InstallmentFlag(): String{
            if (_InstallmentFlag.value.length < _InstallmentFlag.length){
                _InstallmentFlag.value = _InstallmentFlag.value.paddingLeft(_InstallmentFlag.length, " ")
            }
            return _InstallmentFlag.value
        }

        fun RedeemFlag(): String{
            if (_RedeemFlag.value.length < _RedeemFlag.length){
                _RedeemFlag.value = _RedeemFlag.value.paddingLeft(_RedeemFlag.length, " ")
            }
            return _RedeemFlag.value
        }

        fun DCCFlag(): String{
            if (_DCCFlag.value.length < _DCCFlag.length){
                _DCCFlag.value = _DCCFlag.value.paddingLeft(_DCCFlag.length, "N")
            }
            return _DCCFlag.value
        }

        fun InstallmentPlan(): String{
            if (_InstallmentPlan.value.length < _InstallmentPlan.length){
                _InstallmentPlan.value = _InstallmentPlan.value.paddingLeft(_InstallmentPlan.length, " ")
            }
            return _InstallmentPlan.value
        }

        fun InstallmentTenor(): String{
            if (_InstallmentTenor.value.length < _InstallmentTenor.length){
                _InstallmentTenor.value = _InstallmentTenor.value.paddingLeft(_InstallmentTenor.length, " ")
            }
            return _InstallmentTenor.value
        }

        fun GenericData(): String{
            if (_GenericData.value.length < _GenericData.length){
                _GenericData.value = _GenericData.value.paddingLeft(_GenericData.length, " ")
            }
            return _GenericData.value
        }

        fun ReffNumber(): String{
            if (_ReffNumber.value.length < _ReffNumber.length){
                _ReffNumber.value = _ReffNumber.value.paddingLeft(_ReffNumber.length, " ")
            }
            return _ReffNumber.value
        }


        fun Filler(data: String, length: Int): String{
            _Filler.value = data.paddingLeft(length, " ")
            return _Filler.value
        }

        fun String.paddingLeft(maxLength: Int, replacement: String): String {
            var paddingLeft = ""
            val deviation = maxLength - this.length
            for (i in 0 until deviation){
                paddingLeft += replacement
            }

            return paddingLeft + this
        }

    }

    class EDCBCAResponseMessage {
        var _TransType = dataString("0", 2)
        var _TransAmount = dataString("0", 12)
        var _OtherAmount = dataString("0", 12)
        var _PAN = dataString(" ", 19)
        var _ExpiryDate = dataString("0", 4)
        var _RespCode = dataString("0", 2)
        var _RRN = dataString(" ", 12)
        var _ApprovalCode = dataString("0", 6)
        var _Date = dataString("0", 6)
        var _Time = dataString("0", 8)
        var _MerchaintId = dataString("0", 15)
        var _TerminalId = dataString("0", 8)
        var _OfflineFlag = dataString("0", 1)
        var _CardholderName = dataString(" ", 26)
        var _PANCashierCard = dataString(" ", 16)
        var _InvoiceNumber = dataString("0", 6)
        var _BatchNumber = dataString("0", 6)
        var _IssuerId = dataString("0", 2)
        var _InstallmentFlag = dataString("0", 1)
        var _DCCFlag = dataString("0", 1)
        var _RedeemFlag = dataString("0", 1)
        var _InformationAmount = dataString("0", 12)
        var _DCCDecimalPlace = dataString("0", 1)
        var _DCCCurrencyName = dataString(" ", 3)
        var _DCCExchangeRate = dataString("0", 8)
        var _CouponFlag = dataString("0", 1)
        var _Filler = dataString("0", 8)

        fun TransType(data: String): String{
            _TransType.value = data
            return _TransType.value
        }

        fun TransAmount(data: String): String{
            _TransAmount.value = data
            return _TransAmount.value
        }
        fun OtherAmount(data: String): String{
            _OtherAmount.value = data
            return _OtherAmount.value
        }

        fun PAN(data: String): String{
            _PAN.value = data
            return _PAN.value
        }

        fun ExpiryDate(data: String): String{
            _ExpiryDate.value = data
            return _ExpiryDate.value
        }

        fun RespCode(data: String): String{
            _RespCode.value = data
            return _RespCode.value
        }

        fun RRN(data: String): String{
            _RRN.value = data
            return _RRN.value
        }

        fun ApprovalCode(data: String): String{
            _ApprovalCode.value = data
            return _ApprovalCode.value
        }

        fun Date(data: String): String{
            _Date.value = data
            return _Date.value
        }

        fun Time(data: String): String{
            _Time.value = data
            return _Time.value
        }

        fun MerchaintId(data: String): String{
            _MerchaintId.value = data
            return _MerchaintId.value
        }

        fun TerminalId(data: String): String{
            _TerminalId.value = data
            return _TerminalId.value
        }

        fun OfflineFlag(data: String): String{
            _OfflineFlag.value = data
            return _OfflineFlag.value
        }

        fun CardholderName(data: String): String{
            _CardholderName.value = data
            return _CardholderName.value
        }

        fun PANCashierCard(data: String): String{
            _PANCashierCard.value = data
            return _PANCashierCard.value
        }

        fun InvoiceNumber(data: String): String{
            _InvoiceNumber.value = data
            return _InvoiceNumber.value
        }

        fun BatchNumber(data: String): String{
            _BatchNumber.value = data
            return _BatchNumber.value
        }

        fun IssuerId(data: String): String{
            _IssuerId.value = data
            return _IssuerId.value
        }

        fun InstallmentFlag(data: String): String{
            _InstallmentFlag.value = data
            return _InstallmentFlag.value
        }

        fun DCCFlag(data: String): String{
            _DCCFlag.value = data
            return _DCCFlag.value
        }

        fun RedeemFlag(data: String): String{
            _RedeemFlag.value = data
            return _RedeemFlag.value
        }

        fun InformationAmount(data: String): String{
            _InformationAmount.value = data
            return _InformationAmount.value
        }

        fun DCCDecimalPlace(data: String): String{
            _DCCDecimalPlace.value = data
            return _DCCDecimalPlace.value
        }

        fun DCCCurrencyName(data: String): String{
            _DCCCurrencyName.value = data
            return _DCCCurrencyName.value
        }

        fun DCCExchangeRate(data: String): String{
            _DCCExchangeRate.value = data
            return _DCCExchangeRate.value
        }

        fun CouponFlag(data: String): String{
            _CouponFlag.value = data
            return _CouponFlag.value
        }

        fun Filler(data: String): String{
            _Filler.value = data
            return _Filler.value
        }

    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    data class dataString(
        var value: String,
        var length: Int
    )
}