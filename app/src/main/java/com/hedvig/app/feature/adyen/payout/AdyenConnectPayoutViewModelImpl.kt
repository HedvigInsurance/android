package com.hedvig.app.feature.adyen.payout

import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.adyen.payin.AdyenRepository
import kotlinx.coroutines.launch

class AdyenConnectPayoutViewModelImpl(
    private val repository: AdyenRepository
) : AdyenConnectPayoutViewModel() {
    init {
        viewModelScope.launch {
            val response = runCatching { repository.payoutMethods() }
            response.getOrNull()?.data?.let { _payoutMethods.postValue(it.availablePayoutMethods.paymentMethodsResponse) }
        }
    }
}
