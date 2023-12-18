package com.hedvig.android.data.chat.read.timestamp

import com.hedvig.android.auth.event.AuthEventListener

/**
 * Automatically clears the timestamp of the newest message seen when the member logs out
 */
internal class ChatLastMessageSeenClearingAuthEventListener(
  private val chatMessageTimestampStorage: ChatMessageTimestampStorage,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {}

  override suspend fun loggedOut() {
    chatMessageTimestampStorage.clearLatestReadTimestamp()
  }
}
