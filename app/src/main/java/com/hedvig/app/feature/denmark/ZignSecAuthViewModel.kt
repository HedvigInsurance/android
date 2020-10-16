package com.hedvig.app.feature.denmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.AuthState
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DanishAuthViewModel(
    private val repository: DanishAuthRepository
) : ViewModel() {
    protected val _redirectUrl = MutableLiveData<String>()
    protected val _authStatus = MutableLiveData<AuthState>()
    val redirectUrl: LiveData<String> = _redirectUrl
    val authStatus: LiveData<AuthState> = _authStatus

    init {
        viewModelScope.launch {
            repository
                .authStatus()
                .onEach { _authStatus.postValue(it.data?.authStatus?.status) }
                .catch { e(it) }
                .launchIn(this)

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
    }
}
