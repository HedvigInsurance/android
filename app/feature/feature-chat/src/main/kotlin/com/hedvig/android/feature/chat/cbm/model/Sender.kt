package com.hedvig.android.feature.chat.cbm.model

import octopus.type.ChatMessageSender

internal enum class Sender {
  HEDVIG,
  MEMBER,
}

internal fun ChatMessageSender.toSender(): Sender = when (this) {
  ChatMessageSender.MEMBER -> Sender.MEMBER
  ChatMessageSender.HEDVIG -> Sender.HEDVIG
  ChatMessageSender.UNKNOWN__ -> Sender.HEDVIG
}
