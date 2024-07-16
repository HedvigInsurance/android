package com.hedvig.android.data.chat.read.timestamp

import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import octopus.ChatLatestMessageTimestampsQuery

interface ChatLastMessageReadRepository {
  /**
   * Reports the timestamp of the newest message which the member has seen. Ignores it if an already newer timestamp
   * has been reported.
   *
   * TODO remove when CBM is enabled. With it, the stored timestamp happens from the RemoteMediator directly to the DB
   */
  suspend fun storeLatestReadTimestamp(timestamp: Instant)

  /**
   * Returns [true] if the newest message is newer than the last message which the member has already seen.
   * Continuously queries the latest messages to emit the most up-to-date value.
   */
  fun isNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean>
}

internal class ChatLastMessageReadRepositoryImpl(
  private val chatMessageTimestampStorage: ChatMessageTimestampStorage,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val chatDao: ChatDao,
) : ChatLastMessageReadRepository {
  override suspend fun storeLatestReadTimestamp(timestamp: Instant) {
    if (featureManager.isFeatureEnabled(Feature.ENABLE_CBM).first()) return
    val existingTimestamp = chatMessageTimestampStorage.getLatestReadTimestamp()
    if (existingTimestamp != null && existingTimestamp > timestamp) {
      return
    }
    chatMessageTimestampStorage.setLatestReadTimestamp(timestamp)
  }

  override fun isNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean> {
    return featureManager.isFeatureEnabled(Feature.ENABLE_CBM).transformLatest { isCbmEnabled ->
      while (currentCoroutineContext().isActive) {
        if (isCbmEnabled) {
          emit(false) // todo cbm notification dot for home screen
        } else {
          val lastReadMessageTimestamp: Instant? = chatMessageTimestampStorage.getLatestReadTimestamp()
          val messages = apolloClient
            .query(ChatLatestMessageTimestampsQuery())
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .safeExecute()
            .toEither()
            .getOrNull()
            ?.chat
            ?.messages
            ?.toNonEmptyListOrNull()
          if (messages == null) {
            emit(false)
          } else if (lastReadMessageTimestamp == null) {
            // If there are existing messages, but we have seen none of them, there always is an unread message
            emit(true)
          } else {
            val newestChatMessageTimestamp = messages.maxOf { it.sentAt }
            emit(lastReadMessageTimestamp < newestChatMessageTimestamp)
          }
        }
        delay(10.seconds)
      }
    }
  }
}
