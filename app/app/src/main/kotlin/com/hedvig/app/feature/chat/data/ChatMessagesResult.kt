package com.hedvig.app.feature.chat.data

import kotlinx.datetime.Instant

data class ChatMessagesResult(
  val messages: List<ChatMessage>,
  val nextUntil: Instant,
  val hasNext: Boolean,
)
