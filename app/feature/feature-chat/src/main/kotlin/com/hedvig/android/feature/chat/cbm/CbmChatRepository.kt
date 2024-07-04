package com.hedvig.android.feature.chat.cbm

import android.util.Patterns
import androidx.room.withTransaction
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.doNotStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.feature.chat.cbm.database.AppDatabase
import com.hedvig.android.feature.chat.cbm.database.ChatDao
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.toChatMessageEntity
import com.hedvig.android.feature.chat.cbm.model.toSender
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import octopus.ConversationQuery
import octopus.ConversationSendMessageMutation
import octopus.fragment.ChatMessageFileChatMessageFragment
import octopus.fragment.ChatMessageFragment
import octopus.fragment.ChatMessageTextChatMessageFragment

internal class CbmChatRepository(
  private val apolloClient: ApolloClient,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val clock: Clock,
) {
  suspend fun chatMessages(
    conversationId: Uuid,
    pagingToken: PagingToken?,
  ): Either<Throwable?, ChatMessagePageResponse> {
    return internalChatMessages(conversationId, pagingToken).mapLeft { Exception(it) }
  }

  fun pollNewestMessages(conversationId: Uuid): Flow<String> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val newerTokenOrNull = remoteKeyDao.remoteKeyForConversation(conversationId).newerToken?.let {
          PagingToken.NewerToken(it)
        }
        internalChatMessages(conversationId, newerTokenOrNull).fold(
          ifLeft = { errorMessage ->
            emit(errorMessage ?: "Unknown error")
          },
          ifRight = { messagePageResponse ->
            database.withTransaction {
              val existingRemoteKey = remoteKeyDao.remoteKeyForConversation(conversationId)
              remoteKeyDao.insert(existingRemoteKey.copy(newerToken = messagePageResponse.newerToken))
              chatDao.insertAll(messagePageResponse.messages.map { it.toChatMessageEntity(conversationId) })
            }
          },
        )
        delay(5.seconds)
      }
    }
  }

  suspend fun retrySendMessage(conversationId: Uuid, messageId: String): Either<String, CbmChatMessage> {
    val messageToRetry = database.withTransaction {
      val message = chatDao.getFailedMessage(conversationId, messageId)
      chatDao.deleteMessage(conversationId, messageId)
      message
    } ?: return "Message not found".left()
    return sendText(conversationId, messageToRetry.text!!)
  }

  suspend fun sendText(conversationId: Uuid, text: String): Either<String, CbmChatMessage> {
    return sendMessage(conversationId, ConversationInput.Text(text)).onLeft {
      val failedMessage = CbmChatMessage.FailedToBeSent.ChatMessageText(Uuid.randomUUID().toString(), clock.now(), text)
      chatDao.insertAll(listOf(failedMessage.toChatMessageEntity(conversationId)))
    }
  }

  private suspend fun sendMessage(conversationId: Uuid, input: ConversationInput): Either<String, CbmChatMessage> {
    val response = apolloClient
      .mutation(ConversationSendMessageMutation(conversationId.toString(), input.text, input.fileUploadToken))
      .execute()
    return either {
      val data = response.data ?: raise(response.errors.toString())
      data.conversationSendMessage.message?.toChatMessage() ?: raise("Unknown chat message type")
    }
  }

  private suspend fun internalChatMessages(
    conversationId: Uuid,
    pagingToken: PagingToken?,
  ): Either<String?, ChatMessagePageResponse> {
    return either {
      val data = apolloClient
        .query(
          ConversationQuery(
            id = conversationId.toString(),
            newerToken = pagingToken?.newerToken,
            olderToken = pagingToken?.olderToken,
          ),
        ).doNotStore(true)
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither()
        .mapLeft {
          "${it.message} + ${it.throwable?.message}"
        }.bind()
      val messagePage = data.conversation?.messagePage
      ensureNotNull(messagePage) {
        "Empty message page for conversation $conversationId"
      }
      val messages = messagePage.messages.mapNotNull { it.toChatMessage() }
      ChatMessagePageResponse(
        messages = messages,
        newerToken = messagePage.newerToken,
        olderToken = messagePage.olderToken,
      )
    }
  }
}

private sealed interface ConversationInput {
  val text: String?
    get() = (this as? Text)?.text
  val fileUploadToken: String?
    get() = (this as? File)?.fileUploadToken

  data class Text(
    override val text: String,
  ) : ConversationInput

  data class File(
    override val fileUploadToken: String,
  ) : ConversationInput
}

internal sealed interface PagingToken {
  val newerToken: String?
    get() = (this as? NewerToken)?.value
  val olderToken: String?
    get() = (this as? OlderToken)?.value

  data class NewerToken(
    val value: String,
  ) : PagingToken

  data class OlderToken(
    val value: String,
  ) : PagingToken
}

internal data class ChatMessagePageResponse(
  val messages: List<CbmChatMessage>,
  val newerToken: String?,
  val olderToken: String?,
)

private fun ChatMessageFragment.toChatMessage(): CbmChatMessage? = when (this) {
  is ChatMessageFileChatMessageFragment -> CbmChatMessage.ChatMessageFile(
    id = id,
    sender = sender.toSender(),
    sentAt = sentAt,
    url = signedUrl,
    mimeType = when (mimeType) {
      "image/jpeg" -> CbmChatMessage.ChatMessageFile.MimeType.IMAGE
      "image/png" -> CbmChatMessage.ChatMessageFile.MimeType.IMAGE
      "application/pdf" -> CbmChatMessage.ChatMessageFile.MimeType.PDF
      "video/mp4" -> CbmChatMessage.ChatMessageFile.MimeType.MP4
      else -> CbmChatMessage.ChatMessageFile.MimeType.OTHER
    },
  )

  is ChatMessageTextChatMessageFragment -> {
    if (text.isGifUrl()) {
      CbmChatMessage.ChatMessageGif(
        id = id,
        sender = sender.toSender(),
        sentAt = sentAt,
        gifUrl = text,
      )
    } else {
      CbmChatMessage.ChatMessageText(
        id = id,
        sender = sender.toSender(),
        sentAt = sentAt,
        text = text,
      )
    }
  }

  else -> {
//    logcat(LogPriority.WARN) { "Got unknown message type, can not map message:$this" }
    null
  }
}

private fun String.isGifUrl(): Boolean {
  if (!endsWith(".gif")) return false
  return webUrlLinkMatcher.matchEntire(this) != null
}

private val webUrlLinkMatcher: Regex = Patterns.WEB_URL.toRegex()
