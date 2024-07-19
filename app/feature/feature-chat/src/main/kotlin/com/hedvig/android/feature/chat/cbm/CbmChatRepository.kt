package com.hedvig.android.feature.chat.cbm

import android.net.Uri
import android.util.Patterns
import androidx.core.net.toFile
import androidx.room.withTransaction
import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.doNotStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.retrofit.toErrorMessage
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.MEDIA
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.PHOTO
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.TEXT
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.database.ConversationEntity
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.data.chat.database.RemoteKeyEntity
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.toChatMessageEntity
import com.hedvig.android.feature.chat.cbm.model.toSender
import com.hedvig.android.feature.chat.data.BotServiceService
import com.hedvig.android.feature.chat.data.uploadFile
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import octopus.ConversationInfoQuery
import octopus.ConversationQuery
import octopus.ConversationSendMessageMutation
import octopus.ConversationStartMutation
import octopus.ConversationStatusMessageQuery
import octopus.fragment.ChatMessageFileChatMessageFragment
import octopus.fragment.ChatMessageFragment
import octopus.fragment.ChatMessageTextChatMessageFragment
import okhttp3.MediaType.Companion.toMediaType

internal class CbmChatRepository(
  private val apolloClient: ApolloClient,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val conversationDao: ConversationDao,
  private val fileService: FileService,
  private val botServiceService: BotServiceService,
  private val clock: Clock,
) {
  suspend fun createConversation(conversationId: Uuid): Either<ErrorMessage, ConversationInfo.Info> {
    return either {
      apolloClient
        .mutation(ConversationStartMutation(conversationId.toString()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .conversationStart
        .toConversationInfo()
    }
  }

  fun getConversationInfo(conversationId: Uuid): Flow<Either<ErrorMessage, ConversationInfo>> {
    return apolloClient
      .query(ConversationInfoQuery(conversationId.toString()))
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow(::ErrorMessage)
      .map { response ->
        response.map {
          it.conversation.toConversationInfo()
        }
      }
  }

  fun bannerText(conversationId: Uuid): Flow<BannerText?> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val result = apolloClient
          .query(ConversationStatusMessageQuery(conversationId.toString()))
          .safeExecute()
          .toEither(::ErrorMessage)
        when (result) {
          is Left -> {
            delay(10.seconds)
          }

          is Right -> {
            val conversation = result.value.conversation
            emit(
              when {
                conversation == null -> null
                conversation.isOpen == false -> BannerText.ClosedConversation
                conversation.statusMessage != null -> BannerText.Text(conversation.statusMessage)
                else -> {
                  logcat(LogPriority.ERROR) { "Got unknown conversation status message:$conversation" }
                  null
                }
              },
            )
            break
          }
        }
      }
    }
  }

  suspend fun chatMessages(
    conversationId: Uuid,
    pagingToken: PagingToken?,
  ): Either<Throwable, ChatMessagePageResponse> {
    return internalChatMessages(conversationId, pagingToken).mapLeft { Exception(it) }
  }

  fun pollNewestMessages(conversationId: Uuid): Flow<String> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val newerTokenOrNull = remoteKeyDao.remoteKeyForConversation(conversationId)?.newerToken?.let {
          PagingToken.NewerToken(it)
        }
        internalChatMessages(conversationId, newerTokenOrNull).fold(
          ifLeft = { errorMessage ->
            emit(errorMessage)
          },
          ifRight = { messagePageResponse ->
            database.withTransaction {
              val existingRemoteKey = remoteKeyDao.remoteKeyForConversation(conversationId)
                ?: RemoteKeyEntity(conversationId, null, null)
              remoteKeyDao.insert(existingRemoteKey.copy(newerToken = messagePageResponse.newerToken))
              chatDao.insertAll(messagePageResponse.messages.map { it.toChatMessageEntity(conversationId) })
              conversationDao.insertNewLatestTimestampIfApplicable(ConversationEntity(conversationId, clock.now()))
            }
          },
        )
        delay(5.seconds)
      }
    }
  }

  suspend fun retrySendMessage(conversationId: Uuid, messageId: String): Either<String, CbmChatMessage> {
    return either {
      val messageToRetry = chatDao.getFailedMessage(conversationId, messageId)
      ensureNotNull(messageToRetry) {
        logcat(ERROR) { "Tried to retry sending a message which did not exist in the database:$messageId" }
        "Message not found"
      }
      return with(messageToRetry) {
        when {
          failedToSend == TEXT && text != null -> sendText(conversationId, messageToRetry.id, text!!)
          failedToSend == PHOTO && url != null -> sendPhoto(conversationId, messageToRetry.id, Uri.parse(url))
          failedToSend == MEDIA && url != null -> sendMedia(conversationId, messageToRetry.id, Uri.parse(url))
          else -> {
            logcat(ERROR) { "Tried to retry sending a message which had a wrong structure:$messageToRetry" }
            raise("Unknown message type")
          }
        }.onRight {
          chatDao.deleteMessage(conversationId, messageId)
        }
      }
    }
  }

  suspend fun sendText(conversationId: Uuid, messageId: Uuid?, text: String): Either<String, CbmChatMessage> {
    return sendMessage(conversationId, ConversationInput.Text(text)).onLeft {
      val failedMessage = CbmChatMessage.FailedToBeSent.ChatMessageText(
        messageId?.toString() ?: Uuid.randomUUID().toString(), clock.now(), text,
      )
      chatDao.insert(failedMessage.toChatMessageEntity(conversationId))
    }
  }

  suspend fun sendPhoto(conversationId: Uuid, messageId: Uuid?, uri: Uri): Either<String, CbmChatMessage> {
    return either {
      val uploadToken = uploadPhotoToBotService(uri)
      sendMessage(conversationId, ConversationInput.File(uploadToken)).bind()
    }.onLeft {
      val failedMessage =
        CbmChatMessage.FailedToBeSent.ChatMessagePhoto(
          messageId?.toString() ?: Uuid.randomUUID().toString(), clock.now(), uri
        )
      chatDao.insert(failedMessage.toChatMessageEntity(conversationId))
    }
  }

  suspend fun sendMedia(conversationId: Uuid, messageId: Uuid?, uri: Uri): Either<String, CbmChatMessage> {
    return either {
      val uploadToken = uploadMediaToBotService(uri)
      sendMessage(conversationId, ConversationInput.File(uploadToken)).bind()
    }.onLeft {
      val failedMessage =
        CbmChatMessage.FailedToBeSent.ChatMessageMedia(
          messageId?.toString() ?: Uuid.randomUUID().toString(), clock.now(), uri
        )
      chatDao.insert(failedMessage.toChatMessageEntity(conversationId))
    }
  }

  private suspend fun sendMessage(conversationId: Uuid, input: ConversationInput): Either<String, CbmChatMessage> {
    return either {
      val response = apolloClient
        .mutation(ConversationSendMessageMutation(conversationId.toString(), input.text, input.fileUploadToken))
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft { it.toString() }
        .bind()
      val sentMessage = response.conversationSendMessage.message
      ensureNotNull(sentMessage) {
        "Sent message resulted in no response from backend"
      }
      val chatMessage = response.conversationSendMessage.message.toChatMessage()
      ensureNotNull(chatMessage) {
        "Unknown chat message type"
      }
      chatDao.insert(chatMessage.toChatMessageEntity(conversationId))
      conversationDao.insertNewLatestTimestampIfApplicable(ConversationEntity(conversationId, clock.now()))
      chatMessage
    }.onLeft {
      logcat { "Failed to send message, with error message:$it" }
    }
  }

  private suspend fun internalChatMessages(
    conversationId: Uuid,
    pagingToken: PagingToken?,
  ): Either<String, ChatMessagePageResponse> {
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

  private suspend fun Raise<String>.uploadPhotoToBotService(uri: Uri): String {
    val contentType = fileService.getMimeType(uri).toMediaType()
    val file = uri.toFile()
    val uploadToken = botServiceService
      .uploadFile(file, contentType)
      .onLeft {
      }.mapLeft {
        val errorMessage = "Failed to upload file with path:${file.absolutePath}. Error:$it"
        logcat(LogPriority.ERROR) { errorMessage }
        it.toErrorMessage().message ?: errorMessage
      }.bind()
      .firstOrNull()
      ?.uploadToken
    ensureNotNull(uploadToken) { "No upload token" }
    logcat { "Uploaded file with path:${file.absolutePath}. UploadToken:$uploadToken" }
    return uploadToken
  }

  private suspend fun Raise<String>.uploadMediaToBotService(uri: Uri): String {
    val uploadToken = botServiceService
      .uploadFile(fileService.createFormData(uri))
      .mapLeft {
        val errorMessage = "Failed to upload media with uri:$uri. Error:$it"
        logcat(LogPriority.ERROR) { errorMessage }
        it.toErrorMessage().message ?: errorMessage
      }.bind()
      .firstOrNull()
      ?.uploadToken
    ensureNotNull(uploadToken) { "No upload token" }
    logcat { "Uploaded file with uri:$uri. UploadToken:$uploadToken" }
    return uploadToken
  }
}

internal sealed interface ConversationInfo {
  data object NoConversation : ConversationInfo

  data class Info(
    val conversationId: String,
    val title: String,
    val createdAt: Instant,
    val isLegacy: Boolean,
  ) : ConversationInfo
}

private fun octopus.fragment.ConversationInfo?.toConversationInfo(): ConversationInfo {
  return if (this == null) {
    ConversationInfo.NoConversation
  } else {
    ConversationInfo.Info(
      conversationId = id,
      title = title,
      createdAt = createdAt,
      isLegacy = isLegacy,
    )
  }
}

private fun octopus.fragment.ConversationInfo.toConversationInfo(): ConversationInfo.Info {
  return ConversationInfo.Info(
    conversationId = id,
    title = title,
    createdAt = createdAt,
    isLegacy = isLegacy,
  )
}

internal sealed interface BannerText {
  data object ClosedConversation : BannerText

  data class Text(
    val text: String,
  ) : BannerText
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
