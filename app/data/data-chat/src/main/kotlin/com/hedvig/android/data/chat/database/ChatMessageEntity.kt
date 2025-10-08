package com.hedvig.android.data.chat.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid
import kotlin.time.Instant

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
  @ColumnInfo(defaultValue = "0")
  val isBeingSent: Boolean,
  @Embedded
  val banner: ChatMessageEntityBanner?,
) {
  enum class Sender {
    HEDVIG,
    AUTOMATION,
    MEMBER,
  }

  enum class FailedToSendType {
    TEXT,
    PHOTO,
    MEDIA,
  }
}

data class ChatMessageEntityBanner(
  val title: String?,
  val subtitle: String?,
  val detailsTitle: String?,
  val detailsDescription: String?,
  val style: Style,
) {
  enum class Style {
    INFO,
    FANCY_INFO,
  }
}
