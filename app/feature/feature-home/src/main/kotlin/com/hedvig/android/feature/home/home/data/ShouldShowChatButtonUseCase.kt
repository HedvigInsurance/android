package com.hedvig.android.feature.home.home.data

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.watch
import com.apollographql.apollo3.exception.ApolloCompositeException
import com.apollographql.apollo3.exception.CacheMissException
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import octopus.NumberOfChatMessagesQuery
import octopus.type.ChatMessageSender

class ShouldShowChatButtonUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) {
  fun invoke(): Flow<Boolean> {
    return combine(
      isEligibleToShowTheChatIcon(),
      featureManager.isFeatureEnabled(Feature.DISABLE_CHAT),
      featureManager.isFeatureEnabled(Feature.HELP_CENTER),
    ) { isEligibleToShowTheChatIcon, disableChat, showHelpCenter ->
      !shouldHideChatButton(
        disableChat,
        isEligibleToShowTheChatIcon.getOrElse { false },
        showHelpCenter,
      )
    }
  }

  private fun isEligibleToShowTheChatIcon(): Flow<Either<ErrorMessage, Boolean>> {
    return apolloClient.query(NumberOfChatMessagesQuery())
      .watch(fetchThrows = true)
      .map { apolloResponse ->
        either {
          val data = ensureNotNull(apolloResponse.data) {
            ErrorMessage("Home failed to fetch chat history")
          }
          val chatMessages = data.chat.messages.map { message ->
            ChatMessage(
              message.id,
              when (message.sender) {
                ChatMessageSender.MEMBER -> ChatMessage.Sender.MEMBER
                ChatMessageSender.HEDVIG -> ChatMessage.Sender.HEDVIG
                ChatMessageSender.UNKNOWN__ -> ChatMessage.Sender.HEDVIG
              },
            )
          }
          chatMessages.isEligibleToShowTheChatIcon()
        }
      }
      .retryWhen { cause, attempt ->
        val shouldRetry = cause is CacheMissException ||
          (cause is ApolloCompositeException && cause.suppressedExceptions.any { it is CacheMissException })
        if (shouldRetry) {
          emit(ErrorMessage("").left())
          delay(attempt.coerceAtMost(3).seconds)
        }
        shouldRetry
      }
  }

  private fun List<ChatMessage>.isEligibleToShowTheChatIcon(): Boolean {
    // If there are *any* messages from the member, then we should show the chat icon
    if (this.any { it.sender == ChatMessage.Sender.MEMBER }) return true
    // There is always an automatic message sent by Hedvig, therefore we need to check for > 1
    return this.filter { it.sender == ChatMessage.Sender.HEDVIG }.size > 1
  }

  private fun shouldHideChatButton(
    isChatDisabledFromKillSwitch: Boolean,
    isEligibleToShowTheChatIcon: Boolean,
    isHelpCenterEnabled: Boolean,
  ): Boolean {
    // If the feature flag is off, we should hide the chat button regardless of the other conditions
    if (isChatDisabledFromKillSwitch) return true
    // If the help center is disabled, we must always show the chat button, otherwise there is no way to get to the chat
    if (!isHelpCenterEnabled) return false
    return !isEligibleToShowTheChatIcon
  }
}
