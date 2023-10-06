package com.hedvig.app.feature.chat.data

import kotlinx.datetime.LocalDateTime

sealed interface ChatMessage {

  val id: String
  val sender: Sender
  val sentAt: LocalDateTime?

  data class ChatMessageText(
    override val id: String,
    override val sender: Sender,
    override val sentAt: LocalDateTime?,
    val text: String,
  ) : ChatMessage

  data class ChatMessageFile(
    override val id: String,
    override val sender: Sender,
    override val sentAt: LocalDateTime?,
    val url: String,
    val mimeType: String,
  ) : ChatMessage

  enum class Sender {
    HEDVIG, MEMBER;
  }
}
