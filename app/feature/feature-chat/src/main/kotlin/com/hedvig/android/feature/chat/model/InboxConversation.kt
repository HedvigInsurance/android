package com.hedvig.android.feature.chat.model

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

    data class ClaimConversation(
      val claimType: String?,
    ) : Header

    data object ServiceConversation : Header
  }
}
