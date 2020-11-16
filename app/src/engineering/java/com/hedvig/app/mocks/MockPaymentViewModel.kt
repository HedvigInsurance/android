package com.hedvig.app.mocks

import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA

class MockPaymentViewModel : PaymentViewModel() {
    init {
        _paymentData.postValue(PAYMENT_DATA)
        _payinStatusData.postValue(PAYIN_STATUS_DATA_NEEDS_SETUP)
    }
}
