package com.hedvig.android.feature.chat.cbm.model

import android.net.Uri
import com.benasher44.uuid.Uuid
import com.hedvig.android.feature.chat.cbm.database.ChatMessageEntity
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
     * To render a picture/file which failed to be sent, we only got the URI representation of it on-device.
     */
    data class ChatMessageUri(
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
      Uuid.fromString(id),
      conversationId,
      sender.toSender(),
      sentAt,
      null,
      null,
      url,
      mimeType.name,
      false,
    )
    is CbmChatMessage.ChatMessageGif -> ChatMessageEntity(
      Uuid.fromString(id),
      conversationId,
      sender.toSender(),
      sentAt,
      null,
      gifUrl,
      null,
      null,
      false,
    )
    is CbmChatMessage.ChatMessageText -> ChatMessageEntity(
      Uuid.fromString(id),
      conversationId,
      sender.toSender(),
      sentAt,
      text,
      null,
      null,
      null,
      false,
    )
    is CbmChatMessage.FailedToBeSent.ChatMessageText -> ChatMessageEntity(
      Uuid.fromString(id),
      conversationId,
      sender.toSender(),
      sentAt,
      text,
      null,
      null,
      null,
      true,
    )
    is CbmChatMessage.FailedToBeSent.ChatMessageUri -> ChatMessageEntity(
      Uuid.fromString(id),
      conversationId,
      sender.toSender(),
      sentAt,
      null,
      null,
      uri.toString(),
      null,
      true,
    )
  }
}

internal fun ChatMessageEntity.toChatMessage(): CbmChatMessage {
  val sender = sender.toSender()
  return when {
    failedToSend -> {
      if (text != null) {
        CbmChatMessage.FailedToBeSent.ChatMessageText(id.toString(), sentAt, text)
      } else if (url != null) {
        CbmChatMessage.FailedToBeSent.ChatMessageUri(id.toString(), sentAt, Uri.parse(url))
      } else {
        error("")
      }
    }
    text != null -> CbmChatMessage.ChatMessageText(id.toString(), sender, sentAt, text)
    gifUrl != null -> CbmChatMessage.ChatMessageGif(id.toString(), sender, sentAt, gifUrl)
    url != null && mimeType != null -> {
      val mimeType = CbmChatMessage.ChatMessageFile.MimeType.valueOf(mimeType)
      CbmChatMessage.ChatMessageFile(id.toString(), sender, sentAt, url, mimeType)
    }
    else -> error("Unknown ChatMessage type. Message entity:$this")
  }
}
