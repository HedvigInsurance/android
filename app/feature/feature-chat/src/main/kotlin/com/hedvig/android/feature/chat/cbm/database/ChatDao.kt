package com.hedvig.android.feature.chat.cbm.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(messages: List<ChatMessageEntity>)

  /**
   * Clears all remotely fetched messages and leaves the failed to be sent ones in the DB so that even after a refresh
   * they can still be retried
   */
  @Query(
    """
    DELETE FROM chat_messages
    WHERE conversationId LIKE :conversationId 
    """,
  )
  suspend fun clearRemoteMessages(conversationId: Uuid)

  @Query(
    """
    SELECT * FROM chat_messages
    WHERE conversationId LIKE :conversationId
    ORDER BY sentAt DESC
    """,
  )
  fun messages(conversationId: Uuid): PagingSource<Int, ChatMessageEntity>

  @Query(
    """
    SELECT * FROM chat_messages
    WHERE conversationId LIKE :conversationId
    ORDER BY sentAt DESC
    LIMIT 1
    """,
  )
  fun latestMessage(conversationId: Uuid): Flow<ChatMessageEntity?>

  @Query(
    """
    DELETE FROM chat_messages
    WHERE conversationId LIKE :conversationId AND id LIKE :messageId
    """,
  )
  suspend fun deleteMessage(conversationId: Uuid, messageId: String)

  @Query(
    """
    SELECT * FROM chat_messages
    WHERE conversationId LIKE :conversationId AND failedToSend = TRUE AND id LIKE :messageId
    LIMIT 1
    """,
  )
  suspend fun getFailedMessage(conversationId: Uuid, messageId: String): ChatMessageEntity?
}
