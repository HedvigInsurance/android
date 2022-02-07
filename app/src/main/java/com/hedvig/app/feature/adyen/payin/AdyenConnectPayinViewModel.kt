package com.hedvig.app.feature.adyen.payin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.launch

abstract class AdyenConnectPayinViewModel : ViewModel() {
    protected val _paymentMethods = MutableLiveData<PaymentMethodsApiResponse>()
    val paymentMethods: LiveData<PaymentMethodsApiResponse> = _paymentMethods
}

class AdyenConnectPayinViewModelImpl(
    private val adyenRepository: AdyenRepository,
    hAnalytics: HAnalytics,
) : AdyenConnectPayinViewModel() {

    init {
        hAnalytics.screenViewConnectPaymentAdyen()
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
