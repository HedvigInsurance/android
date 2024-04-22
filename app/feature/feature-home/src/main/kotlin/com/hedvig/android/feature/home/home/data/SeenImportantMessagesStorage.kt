package com.hedvig.android.feature.home.home.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SeenImportantMessagesStorage {
  val seenMessages: StateFlow<List<String>>

  fun markMessageAsSeen(id: String)
}

internal class SeenImportantMessagesStorageImpl : SeenImportantMessagesStorage {
  private val storedSeenMessages: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

  override val seenMessages: StateFlow<List<String>>
    get() = storedSeenMessages.asStateFlow()

  override fun markMessageAsSeen(id: String) {
    storedSeenMessages.update { it.plus(id) }
  }
}
