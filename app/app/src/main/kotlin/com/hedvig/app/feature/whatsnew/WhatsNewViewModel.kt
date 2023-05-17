package com.hedvig.app.feature.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.apollo.toEither
import com.hedvig.app.util.LiveEvent
import giraffe.WhatsNewQuery
import kotlinx.coroutines.launch
import slimber.log.e

abstract class WhatsNewViewModel : ViewModel() {
  val news = LiveEvent<WhatsNewQuery.Data>()

  abstract fun fetchNews(sinceVersion: String? = null)

  abstract fun hasSeenNews(version: String)
}

class WhatsNewViewModelImpl(
  private val whatsNewRepository: WhatsNewRepository,
) : WhatsNewViewModel() {

  override fun fetchNews(sinceVersion: String?) {
    viewModelScope.launch {
      when (val response = whatsNewRepository.whatsNew(sinceVersion).toEither()) {
        is Either.Left -> {
          response.value.message?.let { e { it } }
        }
        is Either.Right -> {
          val value = response.value
          news.postValue(value)
        }
      }
    }
  }

  override fun hasSeenNews(version: String) = whatsNewRepository.hasSeenNews(version)
}
