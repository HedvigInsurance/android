package com.hedvig.android.data.chat.read.timestamp

import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import kotlinx.datetime.Instant
import octopus.ChatLatestMessageTimestampsQuery

interface ChatLastMessageReadRepository {
  /**
   * Reports the timestamp of the newest message which the member has seen. Ignores it if an already newer timestamp
   * has been reported.
   */
  suspend fun storeLatestReadTimestamp(timestamp: Instant)

  /**
   * Returns [true] if the newest message is newer than the last message which the member has already seen.
   */
  suspend fun isNewestMessageNewerThanLastReadTimestamp(): Boolean
}

internal class ChatLastMessageReadRepositoryImpl(
  private val chatMessageTimestampStorage: ChatMessageTimestampStorage,
  private val apolloClient: ApolloClient,
) : ChatLastMessageReadRepository {
  override suspend fun storeLatestReadTimestamp(timestamp: Instant) {
    val existingTimestamp = chatMessageTimestampStorage.getLatestReadTimestamp()
    if (existingTimestamp != null && existingTimestamp > timestamp) {
      return
    }
    chatMessageTimestampStorage.setLatestReadTimestamp(timestamp)
  }

  override suspend fun isNewestMessageNewerThanLastReadTimestamp(): Boolean {
    val lastReadMessageTimestamp: Instant? = chatMessageTimestampStorage.getLatestReadTimestamp()
    val messages = apolloClient.query(ChatLatestMessageTimestampsQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute()
      .toEither()
      .getOrNull()
      ?.chat
      ?.messages
      ?.toNonEmptyListOrNull() ?: return false
    if (lastReadMessageTimestamp == null) {
      // If there are existing messages, but we have seen none of them, there always is an unread message
      return true
    }
    val newestChatMessageTimestamp = messages.maxOf { it.sentAt }
    return lastReadMessageTimestamp < newestChatMessageTimestamp
  }
}
