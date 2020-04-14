package com.hedvig.app.feature.whatsnew

import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.util.LiveEvent
import e
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class WhatsNewViewModel(
    private val whatsNewRepository: WhatsNewRepository
) : ViewModel() {

    val news = LiveEvent<WhatsNewQuery.Data>()

    private val disposables = CompositeDisposable()

    fun fetchNews(sinceVersion: String? = null) {
        disposables += whatsNewRepository
            .fetchWhatsNew(sinceVersion)
            .subscribe({ response -> news.postValue(response.data()) }, { e(it) })
    }

    fun hasSeenNews(version: String) = whatsNewRepository.hasSeenNews(version)

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
