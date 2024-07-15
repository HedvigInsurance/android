package com.hedvig.android.data.chat.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey
  val id: Uuid,
  val conversationId: Uuid,
  val sender: Sender,
  val sentAt: Instant,
  val text: String?,
  val gifUrl: String?,
  val url: String?,
  val mimeType: String?,
  val failedToSend: FailedToSendType?,
) {
  enum class Sender {
    HEDVIG,
    MEMBER,
  }

  enum class FailedToSendType {
    TEXT,
    PHOTO,
    MEDIA,
  }
}
