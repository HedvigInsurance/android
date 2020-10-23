package com.hedvig.app.feature.zignsec

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ZignSecAuthViewModel(
    private val repository: ZignSecAuthRepository,
    private val marketProvider: MarketProvider
) : ViewModel() {
    private val _redirectUrl = MutableLiveData<String>()
    private val _authStatus = MutableLiveData<AuthState>()
    val redirectUrl: LiveData<String> = _redirectUrl
    val authStatus: LiveData<AuthState> = _authStatus

    init {
        viewModelScope.launch {
            repository
                .authStatus()
                .onEach { _authStatus.postValue(it.data?.authStatus?.status) }
                .catch { e(it) }
                .launchIn(this)

            marketProvider.market?.let { market ->
                when (market) {
                    Market.NO -> {
                        val response = runCatching {
                            repository
                                .startNorwegianAuthAsync()
                                .await()
                        }
                        if (response.isFailure) {
                            response.exceptionOrNull()?.let { e(it) }
                        }
                        _redirectUrl.postValue(response.getOrNull()?.data?.norwegianBankIdAuth?.redirectUrl)
                    }
                    Market.DK -> {
                        val response = runCatching {
                            repository
                                .startDanishAuthAsync()
                                .await()
                        }
                        if (response.isFailure) {
                            response.exceptionOrNull()?.let { e(it) }
                        }
                        _redirectUrl.postValue(response.getOrNull()?.data?.danishBankIdAuth?.redirectUrl)
                    }
                    else -> {
                        e { "Invalid market used in ${this.javaClass.name}" }
                    }
                }
            }
        }
    }
}
