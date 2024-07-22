package com.hedvig.android.feature.chat.cbm.model

import kotlinx.datetime.Instant

internal data class InboxConversation(
  val conversationId: String,
  val header: Header,
  val latestMessage: LatestMessage?,
  val hasNewMessages: Boolean,
  val createdAt: Instant,
) {
  val lastMessageTimestamp: Instant = latestMessage?.sentAt ?: createdAt

  sealed interface LatestMessage {
    val sender: Sender
    val sentAt: Instant

    data class Text(
      val text: String,
      override val sender: Sender,
      override val sentAt: Instant,
    ) : LatestMessage

    data class File(
      override val sender: Sender,
      override val sentAt: Instant,
    ) : LatestMessage

    data class Unknown(
      override val sender: Sender,
      override val sentAt: Instant,
    ) : LatestMessage
  }

  sealed interface Header {
    object Legacy : Header

    data class Conversation(
      val title: String,
      val subtitle: String?,
    ) : Header
  }
}
