package com.hedvig.app.feature.adyen.payout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.hedvig.app.util.LiveEvent

abstract class AdyenConnectPayoutViewModel : ViewModel() {
    protected val _payoutMethods = MutableLiveData<PaymentMethodsApiResponse>()
    val payoutMethods: LiveData<PaymentMethodsApiResponse> = _payoutMethods
    val shouldClose = LiveEvent<Boolean>()

    fun close() {
        shouldClose.postValue(true)
    }
}
