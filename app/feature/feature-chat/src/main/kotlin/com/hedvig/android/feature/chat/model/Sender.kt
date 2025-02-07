package com.hedvig.android.feature.chat.model

import com.hedvig.android.data.chat.database.ChatMessageEntity
import octopus.type.ChatMessageSender

internal enum class Sender {
  HEDVIG,
  MEMBER,
}

internal fun ChatMessageSender.toSender(): Sender = when (this) {
  ChatMessageSender.MEMBER -> Sender.MEMBER
  ChatMessageSender.HEDVIG -> Sender.HEDVIG
  ChatMessageSender.UNKNOWN__ -> Sender.HEDVIG
  ChatMessageSender.AUTOMATION -> Sender.HEDVIG
}

internal fun ChatMessageEntity.Sender.toSender(): Sender {
  return when (this) {
    ChatMessageEntity.Sender.HEDVIG -> Sender.HEDVIG
    ChatMessageEntity.Sender.MEMBER -> Sender.MEMBER
  }
}

internal fun Sender.toSender(): ChatMessageEntity.Sender {
  return when (this) {
    Sender.HEDVIG -> ChatMessageEntity.Sender.HEDVIG
    Sender.MEMBER -> ChatMessageEntity.Sender.MEMBER
  }
}
