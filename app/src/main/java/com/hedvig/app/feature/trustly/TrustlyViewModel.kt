package com.hedvig.app.feature.trustly

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.launch

abstract class TrustlyViewModel : ViewModel() {
    protected val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
}

class TrustlyViewModelImpl(
    private val repository: TrustlyRepository,
    hAnalytics: HAnalytics,
) : TrustlyViewModel() {
    init {
        hAnalytics.screenViewConnectPaymentTrustly()
        viewModelScope.launch {
            val response = runCatching { repository.startTrustlySession() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.startDirectDebitRegistration?.let { _data.postValue(it) }
        }
    }
}
