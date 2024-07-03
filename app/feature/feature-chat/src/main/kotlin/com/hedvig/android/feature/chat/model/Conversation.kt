package com.hedvig.android.feature.chat.model

internal data class Conversation(
  val conversationId: String,
  val newestMessageForPreview: ChatMessage,
  val hasNewMessages: Boolean,
  val chatMessages: List<ChatMessage>,
  val title: String,
  val subtitle: String,
  val isLegacy: Boolean,
  val statusMessage: String?,
)
