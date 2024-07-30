package com.hedvig.android.data.chat.read.timestamp

import arrow.core.merge
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.data.chat.database.ConversationDao
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
import octopus.CbmChatLatestMessageTimestampsQuery
import octopus.CbmChatLatestMessageTimestampsQuery.Data.CurrentMember
import octopus.ChatLatestMessageTimestampsQuery
import octopus.type.ChatMessageSender.MEMBER

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
  private val conversationDao: ConversationDao,
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
          val isNewestMessageNewerThanLastReadTimestamp = either {
            val currentMember = apolloClient.query(CbmChatLatestMessageTimestampsQuery())
              .fetchPolicy(FetchPolicy.NetworkOnly)
              .safeExecute()
              .toEither()
              .bind()
              .currentMember
            val allNewestHedvigMessages = buildList {
              add(currentMember.legacyConversation?.toNewestHedvigMessage())
              addAll(currentMember.conversations.map { it.toNewestHedvigMessage() })
            }.filterNotNull()
            val backendTimestamps = allNewestHedvigMessages.associate { it.conversationId to it.lastMessageTimestamp }
            val databaseTimestamps: Map<String, Instant> = conversationDao
              .getLatestTimestamps(allNewestHedvigMessages.map { Uuid.fromString(it.conversationId) })
              .associate { it.id.toString() to it.lastMessageReadTimestamp }
            for ((id, backendTimestamp) in backendTimestamps) {
              val databaseTimestamp = databaseTimestamps[id]
              if (databaseTimestamp != null && databaseTimestamp < backendTimestamp) {
                return@either true
              }
            }
            false
          }
            .mapLeft { false }
            .merge()
          emit(isNewestMessageNewerThanLastReadTimestamp)
        } else {
          val lastReadMessageTimestamp: Instant? = chatMessageTimestampStorage.getLatestReadTimestamp()
          val messages = apolloClient
            .query(ChatLatestMessageTimestampsQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
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

/**
 * Represents the timestamp [lastMessageTimestamp] of the newest message sent inside a conversation with
 * [conversationId]. This class *never* represents a message sent by the member.
 */
private data class NewestMessageFromHedvig(val conversationId: String, val lastMessageTimestamp: Instant)

private fun CurrentMember.Conversation.toNewestHedvigMessage(): NewestMessageFromHedvig? {
  if (newestMessage?.sender == MEMBER) return null
  return newestMessage?.sentAt?.let { NewestMessageFromHedvig(id, newestMessage.sentAt) }
}

private fun CurrentMember.LegacyConversation.toNewestHedvigMessage(): NewestMessageFromHedvig? {
  if (newestMessage?.sender == MEMBER) return null
  return newestMessage?.sentAt?.let { NewestMessageFromHedvig(id, newestMessage.sentAt) }
}
