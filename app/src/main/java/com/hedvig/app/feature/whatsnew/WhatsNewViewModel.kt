package com.hedvig.app.feature.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.util.LiveEvent
import e
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class WhatsNewViewModel(
    private val whatsNewRepository: WhatsNewRepository
) : ViewModel() {

    val news = LiveEvent<WhatsNewQuery.Data>()

    private val disposables = CompositeDisposable()

    fun fetchNews(sinceVersion: String? = null) {
        viewModelScope.launch {
            val response = runCatching { whatsNewRepository.fetchWhatsNew(sinceVersion) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.let { news.postValue(it.data()) }
        }
    }

    fun hasSeenNews(version: String) = whatsNewRepository.hasSeenNews(version)

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
