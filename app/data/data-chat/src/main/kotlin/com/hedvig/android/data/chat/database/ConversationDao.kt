package com.hedvig.android.data.chat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
  @Query(
    """
      SELECT * FROM conversations
    """,
  )
  fun getConversations(): Flow<List<ConversationEntity>>

  @Query(
    """
      SELECT * FROM conversations
      WHERE id = :id
    """,
  )
  suspend fun getConversation(id: Uuid): ConversationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConversation(conversationEntity: ConversationEntity)

  @Transaction
  suspend fun insertNewLatestTimestampIfApplicable(conversationEntity: ConversationEntity) {
    val existingTimestamp = getConversation(conversationEntity.id)
    val shouldInsertNewEntity = existingTimestamp == null ||
      existingTimestamp.lastMessageReadTimestamp < conversationEntity.lastMessageReadTimestamp
    if (shouldInsertNewEntity) {
      insertConversation(conversationEntity)
    }
  }
}
