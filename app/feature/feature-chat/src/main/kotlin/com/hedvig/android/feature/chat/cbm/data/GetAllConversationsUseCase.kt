package com.hedvig.android.feature.chat.cbm.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.cbm.model.InboxConversation
import com.hedvig.android.feature.chat.cbm.model.InboxConversation.LatestMessage
import com.hedvig.android.feature.chat.cbm.model.toSender
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
) : GetAllConversationsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<InboxConversation>>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val inboxConversations = either<ErrorMessage, List<InboxConversation>> {
          val response = apolloClient
            .query(ChatConversationsQuery())
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .safeExecute()
            .toEither(::ErrorMessage)
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
    header = if (isLegacy) {
      InboxConversation.Header.Legacy
    } else {
      InboxConversation.Header.Conversation(
        title = title,
        subtitle = subtitle,
      )
    },
    latestMessage = latestMessage,
    hasNewMessages = false, // todo store latest seen message in DB
    createdAt = createdAt,
  )
}
