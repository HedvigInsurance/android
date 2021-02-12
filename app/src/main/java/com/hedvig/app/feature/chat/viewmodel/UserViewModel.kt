package com.hedvig.app.feature.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.feature.chat.data.UserRepository
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

    fun logout(callback: () -> Unit) {
        CoroutineScope(IO).launch {
            val response = runCatching { userRepository.logout() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to log out" } }
                return@launch
            }
            apolloClient.subscriptionManager.reconnect()
            callback()
        }
    }
}
