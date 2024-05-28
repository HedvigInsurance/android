package com.hedvig.android.feature.chat.model

import kotlinx.datetime.Instant

data class ChatMessagesResult(
  val messages: List<ChatMessage>,
  val nextUntil: Instant,
  val hasNext: Boolean,
  val informationMessage: String?,
)
