package com.hedvig.app.feature.norway

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.AuthState
import com.hedvig.app.util.extensions.safeLaunch
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NorwegianAuthenticationViewModel(
    private val repository: NorwegianAuthenticationRepository
) : ViewModel() {
    val redirectUrl = MutableLiveData<String>()
    val authStatus = MutableLiveData<AuthState>()

    init {
        viewModelScope.safeLaunch {
            repository
                .authStatus()
                .onEach { authStatus.postValue(it.data()?.authStatus?.status) }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching {
                repository
                    .startAuthAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
            }

            redirectUrl.postValue(response.getOrNull()?.data()?.norwegianBankIdAuth?.redirectUrl)
        }
    }
}
