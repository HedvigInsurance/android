package com.hedvig.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.BankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class UserViewModel(
    private val userRepository: UserRepository,
    private val apolloClientWrapper: ApolloClientWrapper
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val autoStartToken = MutableLiveData<BankIdAuthMutation.Data>()
    val authStatus = MutableLiveData<AuthStatusSubscription.Data>()

    fun fetchBankIdStartToken() {
        disposables += userRepository
            .subscribeAuthStatus()
            .subscribe({ response ->
                authStatus.postValue(response.data())
            }, { e ->
                Timber.e(e)
            }, {
                //TODO: handle in UI
                Timber.i("subscribeAuthStatus was completed")
            })

        disposables += userRepository
            .fetchAutoStartToken()
            .subscribe({ response ->
                autoStartToken.postValue(response.data())
            }, { error ->
                Timber.e(error)
            })
    }

    fun logout(callback: () -> Unit) {
        disposables += userRepository
            .logout()
            .subscribe({
                apolloClientWrapper.invalidateApolloClient()
                callback()
            }, { error ->
                Timber.e(error, "Failed to log out")
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
