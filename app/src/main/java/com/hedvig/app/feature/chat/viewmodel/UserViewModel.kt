package com.hedvig.app.feature.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.chat.data.UserRepository
import e
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val logoutUserCase: LogoutUseCase,
    private val loginStatusService: LoginStatusService
) : ViewModel() {

    val autoStartToken = MutableLiveData<SwedishBankIdAuthMutation.Data>()
    val authStatus = MutableLiveData<AuthStatusSubscription.Data>()

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events

    sealed class Event {
        object Logout : Event()
        data class Error(val message: String?) : Event()
    }

    fun fetchBankIdStartToken() {
        viewModelScope.launch {
            userRepository
                .subscribeAuthStatus()
                .onEach { response ->
                    response.data?.let { authStatus.postValue(it) }
                }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching { userRepository.fetchAutoStartToken() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { autoStartToken.postValue(it) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            when (val result = logoutUserCase.logout()) {
                is LogoutUseCase.LogoutResult.Error -> {
                    _events.tryEmit(Event.Error(result.message))
                }
                LogoutUseCase.LogoutResult.Success -> {
                    _events.tryEmit(Event.Logout)
                }
            }
        }
    }

    fun onAuthSuccess() {
        loginStatusService.isLoggedIn = true
    }
}
