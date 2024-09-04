package com.hedvig.android.data.conversations

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.CbmNumberOfChatMessagesQuery
import octopus.type.ChatMessageSender

class HasAnyActiveConversationUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(alwaysHitTheNetwork: Boolean = false): Flow<Either<ApolloOperationError, Boolean>> {
    return apolloClient
      .query(CbmNumberOfChatMessagesQuery())
      .fetchPolicy(if (alwaysHitTheNetwork) FetchPolicy.CacheAndNetwork else FetchPolicy.CacheFirst)
      .safeFlow()
      .map { result ->
        either {
          val data = result
            .onLeft { apolloOperationError ->
              logcat(LogPriority.ERROR, apolloOperationError.throwable) {
                "isEligibleToShowTheChatIcon cant determine if the chat icon should be shown. $apolloOperationError"
              }
            }
            .bind()
          val eligibleFromLegacyConversation = data
            .currentMember
            .legacyConversation
            ?.messagePage
            ?.messages
            ?.isEligibleToShowTheChatIcon() == true
          if (eligibleFromLegacyConversation) {
            return@either true
          }
          val conversations = data.currentMember.conversations
          val showChatIcon = conversations.any { conversation ->
            val isOpenConversation = conversation.isOpen
            val hasAnyMessageSent = conversation.newestMessage != null
            isOpenConversation || hasAnyMessageSent
          }
          showChatIcon
        }
      }
  }
}

@Suppress("ktlint:standard:max-line-length")
private fun List<CbmNumberOfChatMessagesQuery.Data.CurrentMember.LegacyConversation.MessagePage.Message>.isEligibleToShowTheChatIcon(): Boolean {
  // If there are *any* messages from the member, then we should show the chat icon
  if (this.any { it.sender == ChatMessageSender.MEMBER }) return true
  // There is always an automatic message sent by Hedvig, therefore we need to check for > 1
  return this.filter { it.sender == ChatMessageSender.HEDVIG }.size > 1
}
