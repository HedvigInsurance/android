package com.hedvig.android.feature.home.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface SeenImportantMessagesStorage {

  val seenMessages: Flow<List<String>>

  fun markMessageAsSeen(id: String)
}

internal class SeenImportantMessagesStorageImpl : SeenImportantMessagesStorage {
  private val storedSeenMessages: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

  override val seenMessages: Flow<List<String>>
    get() = storedSeenMessages

  override fun markMessageAsSeen(id: String) {
    storedSeenMessages.update { it.plus(id) }
  }
}
