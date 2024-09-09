package com.hedvig.android.data.chat.read.timestamp

import arrow.core.merge
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.data.chat.database.ConversationDao
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import octopus.CbmChatLatestMessageTimestampsQuery
import octopus.CbmChatLatestMessageTimestampsQuery.Data.CurrentMember
import octopus.type.ChatMessageSender.MEMBER

interface ChatLastMessageReadRepository {
  /**
   * Returns [true] if the newest message is newer than the last message which the member has already seen.
   * Continuously queries the latest messages to emit the most up-to-date value.
   */
  fun isNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean>
}

internal class ChatLastMessageReadRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val conversationDao: ConversationDao,
) : ChatLastMessageReadRepository {
  override fun isNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val isNewestMessageNewerThanLastReadTimestamp = either {
          val currentMember = apolloClient.query(CbmChatLatestMessageTimestampsQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .safeExecute()
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
