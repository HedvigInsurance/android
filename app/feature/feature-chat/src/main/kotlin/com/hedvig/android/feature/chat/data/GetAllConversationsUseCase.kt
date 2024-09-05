package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.database.asIdToTimestampMap
import com.hedvig.android.feature.chat.model.InboxConversation
import com.hedvig.android.feature.chat.model.InboxConversation.LatestMessage
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.model.toSender
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import octopus.ChatConversationsQuery
import octopus.fragment.ConversationFragment
import octopus.fragment.ConversationFragment.NewestMessage.Companion.asChatMessageFile
import octopus.fragment.ConversationFragment.NewestMessage.Companion.asChatMessageText

internal interface GetAllConversationsUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, List<InboxConversation>>>
}

internal class GetAllConversationsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val conversationDao: ConversationDao,
) : GetAllConversationsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<InboxConversation>>> {
    return combine(
      conversationsFromBackendFlow(),
      conversationDao.getConversations(),
    ) { backendConversations, localConversations ->
      backendConversations to localConversations.asIdToTimestampMap
    }.mapLatest { (backendConversationsResult, idToTimestampMap) ->
      backendConversationsResult.map { backendConversations ->
        backendConversations.map { backendConversation ->
          val newestStoredSeenTimestamp = idToTimestampMap[backendConversation.conversationId]
          val newestMessageInConversation = backendConversation.latestMessage?.sentAt
          val latestMessageIsFromMember = backendConversation.latestMessage?.sender == Sender.MEMBER
          if (newestStoredSeenTimestamp == null || newestMessageInConversation == null || latestMessageIsFromMember) {
            backendConversation
          } else {
            backendConversation.copy(hasNewMessages = newestMessageInConversation > newestStoredSeenTimestamp)
          }
        }
      }
    }
  }

  private fun conversationsFromBackendFlow() = flow {
    while (currentCoroutineContext().isActive) {
      val inboxConversations = either<ErrorMessage, List<InboxConversation>> {
        val response = apolloClient
          .query(ChatConversationsQuery())
          .fetchPolicy(FetchPolicy.NetworkOnly)
          .safeExecute(::ErrorMessage)
          .bind()
        buildList {
          addAll(response.currentMember.conversations.map { it.toInboxConversation(isLegacy = false) })
          response.currentMember.legacyConversation?.let { legacyConversation ->
            add(legacyConversation.toInboxConversation(isLegacy = true))
          }
        }.sortedByDescending { it.latestMessage?.sentAt ?: it.createdAt }
      }
      emit(inboxConversations)
      delay(5.seconds)
    }
  }
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
    hasNewMessages = false,
    createdAt = createdAt,
  )
}
