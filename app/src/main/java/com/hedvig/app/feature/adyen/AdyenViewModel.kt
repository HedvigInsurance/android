package com.hedvig.app.feature.adyen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.hedvig.app.util.extensions.safeLaunch
import e

abstract class AdyenViewModel : ViewModel() {
    abstract val paymentMethods: LiveData<PaymentMethodsApiResponse>

    abstract fun loadPaymentMethods()
}

class AdyenViewModelImpl(
    private val adyenRepository: AdyenRepository
) : AdyenViewModel() {
    override val paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()

    override fun loadPaymentMethods() {
        viewModelScope.safeLaunch {
            val response = kotlin.runCatching {
                adyenRepository
                    .paymentMethodsAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@safeLaunch
            }

            paymentMethods.postValue(
                response.getOrNull()?.data()?.availablePaymentMethods?.paymentMethodsResponse
            )
        }
    }
}
