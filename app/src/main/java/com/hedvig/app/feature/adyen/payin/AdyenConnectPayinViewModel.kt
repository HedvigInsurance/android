package com.hedvig.app.feature.adyen.payin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import e
import kotlinx.coroutines.launch

abstract class AdyenConnectPayinViewModel : ViewModel() {
    protected val _paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()
    val paymentMethods: LiveData<PaymentMethodsApiResponse> = _paymentMethods
}

class AdyenConnectPayinViewModelImpl(
    private val adyenRepository: AdyenRepository
) : AdyenConnectPayinViewModel() {

    init {
        viewModelScope.launch {
            val response = runCatching {
                adyenRepository
                    .paymentMethods()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            response.getOrNull()?.data?.availablePaymentMethods?.paymentMethodsResponse?.let {
                _paymentMethods.postValue(
                    it
                )
            }
        }
    }
}
