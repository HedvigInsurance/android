package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.feature.chat.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant

internal interface GetAllConversationsUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>>
}

internal class GetAllConversationsUseCaseImpl(apolloClient: ApolloClient) : GetAllConversationsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>> {
    // todo! remove mock with real impl
    return flow {
      either<ErrorMessage, List<Conversation>> {
        listOf(mockConversation1, mockConversation2, mockConversationLegacy.copy(isLegacy = true))
      }
    }
  }
}

internal interface GetLegacyConversationUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>>
}

internal class GetLegacyConversationUseCaseImpl(apolloClient: ApolloClient) : GetLegacyConversationUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>> {
    // todo! remove mock with real impl
    return flow {
      either<ErrorMessage, Conversation> {
        mockConversationLegacy.copy(isLegacy = true)
      }
    }
  }
}

private val mockConversation1 = Conversation(
  conversationId = "1",
  newestMessageForPreview = ChatMessage.ChatMessageText(
    "11",
    ChatMessage.Sender.HEDVIG,
    sentAt = Instant.fromEpochSeconds(50, 1),
    text = "Please tell as more about how the phone broke.",
  ),
  hasNewMessages = true,
  chatMessages = listOf(
    ChatMessage.ChatMessageText(
      "11",
      ChatMessage.Sender.HEDVIG,
      sentAt = Instant.fromEpochSeconds(50, 1),
      text = "Please tell as more about how the phone broke.",
    ),
  ),
  title = "Claim",
  subtitle = "Broken phone",
  statusMessage = null,
  isLegacy = false,
)

private val mockConversation2 = Conversation(
  conversationId = "2",
  newestMessageForPreview = ChatMessage.ChatMessageText(
    "15",
    ChatMessage.Sender.MEMBER,
    sentAt = Instant.fromEpochSeconds(10300, 1),
    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed dignissim ex dui. Proin eget lectus rhoncus, iaculis diam vitae, tincidunt leo. Nam ligula lacus, ullamcorper auctor tempus eget, commodo sit amet urna. Proin vulputate libero sapien, nec iaculis tellus commodo id. Donec non odio porta, rutrum urna viverra, finibus odio. Aliquam eu erat pellentesque, blandit nulla ac, elementum mauris. Duis consectetur accumsan dui, et sodales arcu imperdiet accumsan. Nullam fermentum justo vitae dui hendrerit vehicula. Vestibulum suscipit efficitur pellentesque. Cras suscipit suscipit lacus, nec efficitur diam viverra nec. Sed vitae pulvinar est, at bibendum neque. Nulla congue ut arcu sed viverra. Aenean bibendum risus nec lacus malesuada, vel consequat nunc porttitor.\n" +
      "\n",
  ),
  hasNewMessages = false,
  chatMessages = listOf(
    ChatMessage.ChatMessageText(
      "15",
      ChatMessage.Sender.MEMBER,
      sentAt = Instant.fromEpochSeconds(10300, 1),
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed dignissim ex dui. Proin eget lectus rhoncus, iaculis diam vitae, tincidunt leo. Nam ligula lacus, ullamcorper auctor tempus eget, commodo sit amet urna. Proin vulputate libero sapien, nec iaculis tellus commodo id. Donec non odio porta, rutrum urna viverra, finibus odio. Aliquam eu erat pellentesque, blandit nulla ac, elementum mauris. Duis consectetur accumsan dui, et sodales arcu imperdiet accumsan. Nullam fermentum justo vitae dui hendrerit vehicula. Vestibulum suscipit efficitur pellentesque. Cras suscipit suscipit lacus, nec efficitur diam viverra nec. Sed vitae pulvinar est, at bibendum neque. Nulla congue ut arcu sed viverra. Aenean bibendum risus nec lacus malesuada, vel consequat nunc porttitor.\n" +
        "\n.",
    ),
  ),
  title = "Question",
  subtitle = "Termination",
  statusMessage = null,
  isLegacy = false,
)

private val mockConversationLegacy = Conversation(
  conversationId = "0",
  newestMessageForPreview = ChatMessage.ChatMessageText(
    "11",
    ChatMessage.Sender.HEDVIG,
    sentAt = Instant.fromEpochSeconds(50, 1),
    text = "Please tell as more about how the phone broke.",
  ),
  hasNewMessages = false,
  chatMessages = listOf(
    ChatMessage.ChatMessageText(
      "11",
      ChatMessage.Sender.HEDVIG,
      sentAt = Instant.fromEpochSeconds(50, 1),
      text = "Please tell as more about how the phone broke.",
    ),
  ),
  title = "Conversation history",
  subtitle = "",
  statusMessage = null,
  isLegacy = true,
)
