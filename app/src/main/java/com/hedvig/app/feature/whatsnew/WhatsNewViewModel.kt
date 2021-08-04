package com.hedvig.app.feature.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.util.LiveEvent
import e
import kotlinx.coroutines.launch

abstract class WhatsNewViewModel : ViewModel() {
    val news = LiveEvent<WhatsNewQuery.Data>()

    abstract fun fetchNews(sinceVersion: String? = null)

    abstract fun hasSeenNews(version: String)
}

class WhatsNewViewModelImpl(
    private val whatsNewRepository: WhatsNewRepository
) : WhatsNewViewModel() {

    override fun fetchNews(sinceVersion: String?) {
        viewModelScope.launch {
            val response = runCatching { whatsNewRepository.whatsNew(sinceVersion) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { news.postValue(it) }
        }
    }

    override fun hasSeenNews(version: String) = whatsNewRepository.hasSeenNews(version)
}
