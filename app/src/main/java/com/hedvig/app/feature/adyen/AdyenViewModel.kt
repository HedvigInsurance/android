package com.hedvig.app.feature.adyen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.base.model.PaymentMethodsApiResponse

abstract class AdyenViewModel : ViewModel() {
    abstract val paymentMethods: LiveData<PaymentMethodsApiResponse>
}

class AdyenViewModelImpl : AdyenViewModel() {
    override val paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()
}
