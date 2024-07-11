package com.hedvig.android.feature.chat.cbm.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant

@Entity(tableName = "conversations")
data class ConversationEntity(
  @PrimaryKey
  val id: Uuid,
  val lastMessageReadTimestamp: Instant,
)

val List<ConversationEntity>.asIdToTimestampMap: Map<String, Instant>
  get() = associate { it.id.toString() to it.lastMessageReadTimestamp }
