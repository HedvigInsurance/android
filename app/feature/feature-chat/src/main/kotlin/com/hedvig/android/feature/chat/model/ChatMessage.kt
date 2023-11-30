package com.hedvig.android.feature.chat.model

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
      PDF,
      OTHER,
    }
  }

  enum class Sender {
    HEDVIG,
    MEMBER,
  }
}
