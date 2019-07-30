package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.feature.chat.ChatRepository
import com.hedvig.app.feature.claims.data.ClaimsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ClaimsViewModel(
    private val claimsRepository: ClaimsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val data: MutableLiveData<CommonClaimQuery.Data> = MutableLiveData()
    val selectedSubViewData: MutableLiveData<CommonClaimQuery.CommonClaim> = MutableLiveData()

    private val disposables = CompositeDisposable()

    init {
        fetchCommonClaims()
    }

    fun fetchCommonClaims() {
        disposables += claimsRepository.fetchCommonClaims().subscribe(
            { data.postValue(it) },
            { error ->
                Timber.e(error, "Failed to fetch claims data")
            })
    }

    fun setSelectedSubViewData(selectedSubView: CommonClaimQuery.CommonClaim) =
        selectedSubViewData.postValue(selectedSubView)

    fun triggerClaimsChat(claimTypeId: String? = null, done: () -> Unit) {
        disposables += claimsRepository
            .triggerClaimsChat(claimTypeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ done() }, { Timber.e(it) })
    }

    fun triggerFreeTextChat(done: () -> Unit) {
        disposables += chatRepository
            .triggerFreeTextChat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ done() }, { Timber.e(it) })
    }

    fun triggerCallMeChat(done: () -> Unit) {
        disposables += claimsRepository
            .triggerCallMeChat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ done() }, { Timber.e(it) })
    }
}
