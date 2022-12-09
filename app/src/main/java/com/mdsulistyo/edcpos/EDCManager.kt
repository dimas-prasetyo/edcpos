package com.mdsulistyo.edcpos


class EDCManager {

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

    fun SendSettlementRequestToEDCBCA(): String {
        val requestmsg = EDCBCARequestMessage
        requestmsg.TransType("10") //settlement
        requestmsg.InstallmentFlag("Y")
        requestmsg.RedeemFlag("Y")
        requestmsg.DCCFlag("N")
        requestmsg.InstallmentPlan("001")
        requestmsg.InstallmentTenor("06")

        return SendRequestToEDCBCA(requestmsg)
    }

    private fun SendRequestToEDCBCA(requestmsg: EDCBCARequestMessage): String {
        try {
            val msg_frame = StringBuilder()
            val msg_data = StringBuilder()

            msg_data.append(requestmsg._TransType.value) //trans type
            msg_data.append(requestmsg._TransAmount.value) //trans amount
            msg_data.append(requestmsg._OtherAmount.value) // other amount
            msg_data.append(requestmsg._PAN.value) // PAN
            msg_data.append(requestmsg._ExpiryDate.value) // Expire date
            msg_data.append(requestmsg._CancelReason.value) // cancel reason
            msg_data.append(requestmsg._InvoiceNumber.value) // invoice number
            msg_data.append(requestmsg._AuthorIDResponse.value) // author ID respon
            msg_data.append(requestmsg._InstallmentFlag.value) // installment Flag
            msg_data.append(requestmsg._RedeemFlag.value) // redeem indicator
            msg_data.append(requestmsg._DCCFlag.value) // DCC flag
            msg_data.append(requestmsg._InstallmentPlan.value) // installment plan
            msg_data.append(requestmsg._InstallmentTenor.value) // installment tenor
            msg_data.append(requestmsg._GenericData.value) // generic data
            msg_data.append(requestmsg.Filler("", 149 - msg_data.toString().length))
            //msg_data.Append("000000000000");

            val builder: StringBuilder = StringBuilder(msg_data.length * 2)

            /*msg_data.toString().forEach { it ->
                println(it)
            }*/

            for (b in msg_data.toString()) {
                //val st = String.format("%02X", b)
                //builder.append(st)
            }
            return ""

        } catch (e: Exception){
            println("error: " + e.message)
            return "error: " + e.message
        }
    }


    object EDCBCARequestMessage {
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

        fun TransType(data: String): String{
            if (data.length == _TransType.length){
                _TransType.value = data
            }
            return _TransType.value
        }

        fun TransAmount(data: String): String{
            if (data.length <= _TransAmount.length){
                _TransAmount.value = data.paddingLeft(_TransAmount.length, "0")
            }
            return _TransAmount.value
        }

        fun OtherAmount(data: String): String{
            if (data.length <= _OtherAmount.length){
                _OtherAmount.value = data.paddingLeft(_OtherAmount.length, "0")
            }
            return _OtherAmount.value
        }

        fun PAN(data: String): String{
            if (data.length <= _PAN.length){
                _PAN.value = data.paddingLeft(_PAN.length, " ")
            }
            return _PAN.value
        }

        fun ExpiryDate(data: String): String{
            if (data.length <= _ExpiryDate.length){
                _ExpiryDate.value = data.paddingLeft(_ExpiryDate.length, "0")
            }
            return _ExpiryDate.value
        }


        fun CancelReason(data: String): String{
            if (data.length <= _CancelReason.length){
                _CancelReason.value = data.paddingLeft(_CancelReason.length, "0")
            }
            return _CancelReason.value
        }

        fun InvoiceNumber(data: String): String{
            if (data.length <= _InvoiceNumber.length){
                _InvoiceNumber.value = data.paddingLeft(_InvoiceNumber.length, "0")
            }
            return _InvoiceNumber.value
        }

        fun AuthorIDResponse(data: String): String{
            if (data.length <= _AuthorIDResponse.length){
                _AuthorIDResponse.value = data.paddingLeft(_AuthorIDResponse.length, " ")
            }
            return _AuthorIDResponse.value
        }

        fun InstallmentFlag(data: String): String{
            if (data.length <= _InstallmentFlag.length){
                _InstallmentFlag.value = data.paddingLeft(_InstallmentFlag.length, " ")
            }
            return _InstallmentFlag.value
        }

        fun RedeemFlag(data: String): String{
            if (data.length <= _RedeemFlag.length){
                _RedeemFlag.value = data.paddingLeft(_RedeemFlag.length, " ")
            }
            return _RedeemFlag.value
        }

        fun DCCFlag(data: String): String{
            if (data.length <= _DCCFlag.length){
                _DCCFlag.value = data.paddingLeft(_DCCFlag.length, "N")
            }
            return _DCCFlag.value
        }

        fun InstallmentPlan(data: String): String{
            if (data.length <= _InstallmentPlan.length){
                _InstallmentPlan.value = data.paddingLeft(_InstallmentPlan.length, " ")
            }
            return _InstallmentPlan.value
        }

        fun InstallmentTenor(data: String): String{
            if (data.length <= _InstallmentTenor.length){
                _InstallmentTenor.value = data.paddingLeft(_InstallmentTenor.length, " ")
            }
            return _InstallmentTenor.value
        }

        fun GenericData(data: String): String{
            if (data.length <= _GenericData.length){
                _GenericData.value = data.paddingLeft(_GenericData.length, " ")
            }
            return _GenericData.value
        }

        fun ReffNumber(data: String): String{
            if (data.length <= _ReffNumber.length){
                _ReffNumber.value = data.paddingLeft(_ReffNumber.length, " ")
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


    object EDCBCAResponseMessage {
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


    data class dataString(
        var value: String,
        var length: Int
    )
}