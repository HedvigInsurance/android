package com.hedvig.android.feature.chat.cbm.model

import android.net.Uri
import com.benasher44.uuid.Uuid
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.MEDIA
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.PHOTO
import com.hedvig.android.data.chat.database.ChatMessageEntity.FailedToSendType.TEXT
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded.LatestChatMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.Instant

internal sealed interface CbmChatMessage {
  val id: String
  val sender: Sender
  val sentAt: Instant

  data class ChatMessageText(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val text: String,
  ) : CbmChatMessage

  data class ChatMessageGif(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val gifUrl: String,
  ) : CbmChatMessage

  data class ChatMessageFile(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val url: String,
    val mimeType: MimeType,
  ) : CbmChatMessage {
    enum class MimeType {
      IMAGE,
      MP4,
      PDF,
      OTHER,
    }
  }

  /**
   * A message which failed to be sent due to network errors, but should be retryable.
   */
  sealed interface FailedToBeSent : CbmChatMessage {
    /**
     * To render a picture which failed to be sent, we only got the URI representation of it on-device.
     */
    data class ChatMessagePhoto(
      override val id: String,
      override val sentAt: Instant,
      val uri: Uri,
    ) : FailedToBeSent {
      override val sender: Sender = Sender.MEMBER
    }

    /**
     * To render a picture which failed to be sent, we only got the URI representation of it, which can not be turn into
     * a file. We must read the Uri contents instead.
     */
    data class ChatMessageMedia(
      override val id: String,
      override val sentAt: Instant,
      val uri: Uri,
    ) : FailedToBeSent {
      override val sender: Sender = Sender.MEMBER
    }

    data class ChatMessageText(
      override val id: String,
      override val sentAt: Instant,
      val text: String,
    ) : FailedToBeSent {
      override val sender: Sender = Sender.MEMBER
    }
  }
}

internal fun CbmChatMessage.toChatMessageEntity(conversationId: Uuid): ChatMessageEntity {
  return when (this) {
    is CbmChatMessage.ChatMessageFile -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = url,
      mimeType = mimeType.name,
      failedToSend = null,
    )

    is CbmChatMessage.ChatMessageGif -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = gifUrl,
      url = null,
      mimeType = null,
      failedToSend = null,
    )

    is CbmChatMessage.ChatMessageText -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = text,
      gifUrl = null,
      url = null,
      mimeType = null,
      failedToSend = null,
    )

    is CbmChatMessage.FailedToBeSent.ChatMessageText -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = text,
      gifUrl = null,
      url = null,
      mimeType = null,
      failedToSend = TEXT,
    )

    is CbmChatMessage.FailedToBeSent.ChatMessagePhoto -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = uri.toString(),
      mimeType = null,
      failedToSend = PHOTO,
    )

    is CbmChatMessage.FailedToBeSent.ChatMessageMedia -> ChatMessageEntity(
      id = Uuid.fromString(id),
      conversationId = conversationId,
      sender = sender.toSender(),
      sentAt = sentAt,
      text = null,
      gifUrl = null,
      url = uri.toString(),
      mimeType = null,
      failedToSend = MEDIA,
    )
  }
}

internal fun ChatMessageEntity.toChatMessage(): CbmChatMessage? {
  val sender = sender.toSender()
  return when {
    failedToSend != null -> {
      when {
        failedToSend == TEXT && text != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessageText(id.toString(), sentAt, text!!.trim())
        }

        failedToSend == PHOTO && url != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessagePhoto(id.toString(), sentAt, Uri.parse(url))
        }

        failedToSend == MEDIA && url != null -> {
          CbmChatMessage.FailedToBeSent.ChatMessageMedia(id.toString(), sentAt, Uri.parse(url))
        }

        else -> {
          logcat(LogPriority.ERROR) {
            "Tried to map a failed to be sent entity to ChatMessage which does not fit any case. Entity:$this"
          }
          null
        }
      }
    }

    text != null -> CbmChatMessage.ChatMessageText(id.toString(), sender, sentAt, text!!.trim())
    gifUrl != null -> CbmChatMessage.ChatMessageGif(id.toString(), sender, sentAt, gifUrl!!)
    url != null && mimeType != null -> {
      val mimeType = CbmChatMessage.ChatMessageFile.MimeType.valueOf(mimeType!!)
      CbmChatMessage.ChatMessageFile(id.toString(), sender, sentAt, url!!, mimeType)
    }

    else -> error("Unknown ChatMessage type. Message entity:$this")
  }
}

internal fun ChatMessageEntity.toLatestChatMessage(): LatestChatMessage {
  return LatestChatMessage(id, this.sender.toSender())
}
