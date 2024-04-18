package com.hedvig.android.feature.home.home.data

import kotlinx.coroutines.flow.MutableStateFlow

interface ImportantMessagesSeer {
  fun hasSeenMessage(id: String): Boolean

  fun markMessageAsSeen(id: String)
}

internal class ImportantMessagesSeerImpl : ImportantMessagesSeer {
  private val storedSeenMessages: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

  override fun hasSeenMessage(id: String): Boolean {
    return storedSeenMessages.value.contains(id)
  }

  override fun markMessageAsSeen(id: String) {
    storedSeenMessages.update { it.plus(id) }
  }
}
