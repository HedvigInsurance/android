package com.hedvig.app.authenticate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val logoutUserCase: LogoutUseCase,
    private val loginStatusService: LoginStatusService,
    private val trackingFacade: TrackingFacade,
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    val autoStartToken = MutableLiveData<SwedishBankIdAuthMutation.Data>()
    val authStatus = MutableLiveData<AuthStatusSubscription.Data>()

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

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
                    _events.trySend(Event.Error(result.message))
                }
                LogoutUseCase.LogoutResult.Success -> {
                    hAnalytics.loggedOut()
                    _events.trySend(Event.Logout)
                }
            }
        }
    }

    fun onAuthSuccess() {
        trackingFacade.setProperty("user_state", "logged_in")
        hAnalytics.loggedIn()
        loginStatusService.isLoggedIn = true
    }
}
