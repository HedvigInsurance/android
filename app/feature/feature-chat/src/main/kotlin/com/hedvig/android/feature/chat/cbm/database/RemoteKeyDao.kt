package com.hedvig.android.feature.chat.cbm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benasher44.uuid.Uuid

@Dao
interface RemoteKeyDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(remoteKeyEntity: RemoteKeyEntity)

  @Query(
    """
    SELECT * FROM remote_keys
    WHERE conversationId = :conversationId
    """,
  )
  suspend fun remoteKeyForConversation(conversationId: Uuid): RemoteKeyEntity?

  @Query(
    """
    DELETE FROM remote_keys 
    WHERE conversationId = :conversationId
    """,
  )
  suspend fun deleteAllForConversation(conversationId: Uuid)
}
