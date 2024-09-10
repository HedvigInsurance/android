package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy.CacheAndNetwork
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.feature.chat.model.InboxConversation
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage
import com.hedvig.android.feature.chat.model.toSender
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import octopus.ChatConversationsQuery
import octopus.ChatConversationsQuery.Data.CurrentMember.LegacyConversation
import octopus.fragment.ConversationFragment
import octopus.fragment.ConversationFragment.NewestMessage.Companion.asChatMessageFile
import octopus.fragment.ConversationFragment.NewestMessage.Companion.asChatMessageText

internal interface GetAllConversationsUseCase {
  suspend fun invoke(): Flow<Either<ApolloOperationError, List<InboxConversation>>>
}

internal class GetAllConversationsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetAllConversationsUseCase {
  override suspend fun invoke(): Flow<Either<ApolloOperationError, List<InboxConversation>>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient
            .query(ChatConversationsQuery())
            .fetchPolicy(CacheAndNetwork)
            .safeFlow()
            .map { response ->
              val inboxConversations = either {
                val currentMember = response.bind().currentMember
                val allConversations = buildList {
                  addAll(currentMember.conversations.map { it.toInboxConversation(isLegacy = false) })
                  currentMember.legacyConversation?.let<LegacyConversation, Unit> { legacyConversation ->
                    add(legacyConversation.toInboxConversation(isLegacy = true))
                  }
                }
                val (closedConversationsWithoutNewMessages, openConversations) = allConversations.partition {
                  it.isClosed && !it.hasNewMessages
                }
                openConversations.sortByLastMessageTimestamp() +
                  closedConversationsWithoutNewMessages.sortByLastMessageTimestamp()
              }
              inboxConversations
            },
        )
        delay(5.seconds)
      }
    }
  }
}

private fun List<InboxConversation>.sortByLastMessageTimestamp(): List<InboxConversation> {
  return this.sortedBy { it.latestMessage?.sentAt ?: it.createdAt }
}

private fun ConversationFragment.toInboxConversation(isLegacy: Boolean): InboxConversation {
  val newestMessage = newestMessage
  val latestMessage = run {
    if (newestMessage == null) return@run null
    newestMessage.asChatMessageText()?.let { textMessage ->
      return@run LatestMessage.Text(textMessage.text, textMessage.sender.toSender(), textMessage.sentAt)
    }
    newestMessage.asChatMessageFile()?.let { fileMessage ->
      return@run LatestMessage.File(fileMessage.sender.toSender(), fileMessage.sentAt)
    }
    LatestMessage.Unknown(newestMessage.sender.toSender(), newestMessage.sentAt)
  }
  return InboxConversation(
    conversationId = id,
    header = when {
      isLegacy -> InboxConversation.Header.Legacy
      claim != null -> InboxConversation.Header.ClaimConversation(claim!!.claimType)
      else -> InboxConversation.Header.ServiceConversation
    },
    latestMessage = latestMessage,
    hasNewMessages = unreadMessageCount > 0,
    createdAt = createdAt,
    isClosed = !isOpen,
  )
}
