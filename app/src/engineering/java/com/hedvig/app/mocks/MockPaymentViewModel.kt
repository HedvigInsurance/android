package com.hedvig.app.mocks

import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA

class MockPaymentViewModel : PaymentViewModel() {
    init {
        _paymentData.postValue(paymentData)
        _payinStatusData.postValue(payinStatusData)
    }

    companion object {
        var paymentData = PAYMENT_DATA
        var payinStatusData = PAYIN_STATUS_DATA_ACTIVE
    }
}
