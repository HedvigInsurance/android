package com.hedvig.android.feature.chat.cbm.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
  @PrimaryKey
  val conversationId: Uuid,
  val olderToken: String?,
  val newerToken: String?,
)
