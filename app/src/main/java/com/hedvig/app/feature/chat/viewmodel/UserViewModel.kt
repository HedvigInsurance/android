package com.hedvig.app.feature.chat.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.extensions.setAuthenticationToken
import e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val apolloClient: ApolloClient,
    private val loginStatusService: LoginStatusService,
) : ViewModel() {

    val autoStartToken = MutableLiveData<SwedishBankIdAuthMutation.Data>()
    val authStatus = MutableLiveData<AuthStatusSubscription.Data>()

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

    fun logout(context: Context, callback: () -> Unit) {
        CoroutineScope(IO).launch {
            val response = runCatching { userRepository.logout() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to log out" } }
                return@launch
            }

            loginStatusService.isViewingOffer = false
            loginStatusService.isLoggedIn = false

            context.setAuthenticationToken(null)

            apolloClient.subscriptionManager.reconnect()
            callback()
        }
    }

    fun onAuthSuccess() {
        loginStatusService.isLoggedIn = true
    }
}
