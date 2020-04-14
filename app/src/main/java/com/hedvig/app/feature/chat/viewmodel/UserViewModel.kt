package com.hedvig.app.feature.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.chat.data.UserRepository
import e
import i
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class UserViewModel(
    private val userRepository: UserRepository,
    private val apolloClientWrapper: ApolloClientWrapper
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val autoStartToken = MutableLiveData<SwedishBankIdAuthMutation.Data>()
    val authStatus = MutableLiveData<AuthStatusSubscription.Data>()

    fun fetchBankIdStartToken() {
        disposables += userRepository
            .subscribeAuthStatus()
            .subscribe({ response ->
                authStatus.postValue(response.data())
            }, { e ->
                e(e)
            }, {
                //TODO: handle in UI
                i { "subscribeAuthStatus was completed" }
            })

        disposables += userRepository
            .fetchAutoStartToken()
            .subscribe({ response ->
                autoStartToken.postValue(response.data())
            }, { error ->
                e(error)
            })
    }

    fun logout(callback: () -> Unit) {
        disposables += userRepository
            .logout()
            .subscribe({
                apolloClientWrapper.invalidateApolloClient()
                callback()
            }, { error ->
                e { "$error Failed to log out" }
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
