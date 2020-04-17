package com.hedvig.app.feature.adyen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import e
import kotlinx.coroutines.launch

abstract class AdyenViewModel : ViewModel() {
    abstract val paymentMethods: LiveData<PaymentMethodsApiResponse>

    abstract fun loadPaymentMethods()
}

class AdyenViewModelImpl(
    private val adyenRepository: AdyenRepository
) : AdyenViewModel() {
    override val paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()

    override fun loadPaymentMethods() {
        viewModelScope.launch {
            val response = kotlin.runCatching {
                adyenRepository
                    .paymentMethodsAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            paymentMethods.postValue(
                response.getOrNull()?.data()?.availablePaymentMethods?.paymentMethodsResponse
            )
        }
    }
}
