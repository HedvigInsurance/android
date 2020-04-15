package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ClaimsViewModel(
    private val claimsRepository: ClaimsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val data: MutableLiveData<CommonClaimQuery.Data> = MutableLiveData()

    private val disposables = CompositeDisposable()

    init {
        fetchCommonClaims()
    }

    fun fetchCommonClaims() {
        disposables += claimsRepository.fetchCommonClaims().subscribe(
            { data.postValue(it) },
            { error ->
                e { "$error Failed to fetch claims data" }
            })
    }

    fun triggerClaimsChat(claimTypeId: String? = null, done: () -> Unit) {
        disposables += claimsRepository
            .triggerClaimsChat(claimTypeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ done() }, { e(it) })
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        viewModelScope.launch {
            chatRepository
                .triggerFreeTextChat()
                .onEach { done() }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    fun triggerCallMeChat(done: () -> Unit) {
        disposables += claimsRepository
            .triggerCallMeChat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ done() }, { e(it) })
    }
}
