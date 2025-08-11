package com.hedvig.android.data.chat.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benasher44.uuid.Uuid
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(message: ChatMessageEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(messages: List<ChatMessageEntity>)

  /**
   * Clears all remotely fetched messages and leaves the failed to be sent ones in the DB so that even after a refresh
   * they can still be retried. Does remove old messages that failed to be sent according to [deleteUnsentMessagesOlderThan].
   */
  @Query(
    """
    DELETE FROM chat_messages
    WHERE conversationId LIKE :conversationId 
        AND (failedToSend IS NULL OR sentAt <= :deleteUnsentMessagesOlderThan)
    """,
  )
  suspend fun clearRemoteMessagesAndOldUnsentMessages(conversationId: Uuid, deleteUnsentMessagesOlderThan: Instant)

  @Query(
    """
    SELECT * FROM chat_messages
    WHERE conversationId LIKE :conversationId
    ORDER BY isBeingSent DESC, sentAt DESC 
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
    SELECT id FROM chat_messages
    WHERE conversationId LIKE :conversationId
        AND failedToSend IS NULL
        AND isBeingSent = 0
    ORDER BY sentAt DESC
    LIMIT 1
    """,
  )
  fun lastDeliveredMessage(conversationId: Uuid): Flow<Uuid?>

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
    WHERE conversationId LIKE :conversationId
        AND failedToSend IS NOT NULL 
        AND id LIKE :messageId
    LIMIT 1
    """,
  )
  suspend fun getFailedMessage(conversationId: Uuid, messageId: String): ChatMessageEntity?
}
