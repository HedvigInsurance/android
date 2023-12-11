package com.hedvig.android.feature.chat.model

import android.net.Uri
import kotlinx.datetime.Instant

sealed interface ChatMessage {
  val id: String
  val sender: Sender
  val sentAt: Instant

  data class ChatMessageText(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val text: String,
  ) : ChatMessage

  data class ChatMessageGif(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val gifUrl: String,
  ) : ChatMessage

  data class ChatMessageFile(
    override val id: String,
    override val sender: Sender,
    override val sentAt: Instant,
    val url: String,
    val mimeType: MimeType,
  ) : ChatMessage {
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
  sealed interface FailedToBeSent : ChatMessage {
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

  enum class Sender {
    HEDVIG,
    MEMBER,
  }
}
