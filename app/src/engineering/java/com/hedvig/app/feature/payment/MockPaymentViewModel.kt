package com.hedvig.app.feature.payment

import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_TRUSTLY_CONNECTED

class MockPaymentViewModel : PaymentViewModel() {
    init {
        _paymentData.postValue(paymentData)
        _payinStatusData.postValue(payinStatusData)
    }

    companion object {
        var paymentData = PAYMENT_DATA_TRUSTLY_CONNECTED
        var payinStatusData = PAYIN_STATUS_DATA_ACTIVE
    }

    override fun load() {
        _paymentData.postValue(paymentData)
    }
}
