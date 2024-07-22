package com.hedvig.android.feature.chat.cbm.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import arrow.core.getOrElse
import com.benasher44.uuid.Uuid
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.database.ConversationEntity
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.data.chat.database.RemoteKeyEntity
import com.hedvig.android.feature.chat.cbm.CbmChatRepository
import com.hedvig.android.feature.chat.cbm.PagingToken
import com.hedvig.android.feature.chat.cbm.model.toChatMessageEntity
import com.hedvig.android.logger.LogPriority.VERBOSE
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock

@OptIn(ExperimentalPagingApi::class)
internal class ChatRemoteMediator(
  private val conversationId: Uuid,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val conversationDao: ConversationDao,
  private val chatRepository: CbmChatRepository,
  private val clock: Clock,
) : RemoteMediator<Int, ChatMessageEntity>() {
  override suspend fun load(loadType: LoadType, state: PagingState<Int, ChatMessageEntity>): MediatorResult {
    logcat(VERBOSE) { "ChatRemoteMediator: called with loadType: $loadType and state: $state" }
    val pagingToken = when (loadType) {
      LoadType.REFRESH -> null
      LoadType.PREPEND -> {
        val newerToken = remoteKeyDao.remoteKeyForConversation(conversationId)?.newerToken
        newerToken ?: return MediatorResult.Success(endOfPaginationReached = true)
        PagingToken.NewerToken(newerToken)
      }

      LoadType.APPEND -> {
        val olderToken = remoteKeyDao.remoteKeyForConversation(conversationId)?.olderToken
        olderToken ?: return MediatorResult.Success(endOfPaginationReached = true)
        PagingToken.OlderToken(olderToken)
      }
    }
    val response = chatRepository.chatMessages(conversationId, pagingToken).getOrElse {
      logcat { "ChatRemoteMediator: Failed to fetch chat messages: $it [MediatorResult.Error(it)]" }
      return MediatorResult.Error(it)
    }
    conversationDao.insertNewLatestTimestampIfApplicable(ConversationEntity(conversationId, clock.now()))
    // The mediator gets a [LoadType.REFRESH] request when we jump enough items to trigger the jumpThreshold of the
    // Pager. This is distinguished from the initial Refresh by seeing if we already have pages loaded.
    // This normally clears the entire cache, but we do not want to do that here, so that already loaded messages are
    // kept in the DB and we can simply scroll up again without re-fetching everything.
    val isRefreshingDueToJumping = loadType == LoadType.REFRESH && state.pages.isNotEmpty()
    if (isRefreshingDueToJumping) {
      logcat { "ChatRemoteMediator: Refreshing due to jumping" }
      database.withTransaction {
        val existingOlderToken = remoteKeyDao.remoteKeyForConversation(conversationId)?.olderToken
        remoteKeyDao.insert(RemoteKeyEntity(conversationId, existingOlderToken, response.newerToken))
        chatDao.insertAll(response.messages.map { it.toChatMessageEntity(conversationId) })
      }
    } else {
      database.withTransaction {
        if (loadType == LoadType.REFRESH) {
          chatDao.clearRemoteMessagesAndOldUnsentMessages(conversationId, clock.now().minus(12.hours))
          remoteKeyDao.deleteAllForConversation(conversationId)
        }
        val remoteKeyEntityToSave = when (loadType) {
          LoadType.REFRESH -> RemoteKeyEntity(conversationId, response.olderToken, response.newerToken)
          LoadType.PREPEND -> {
            val existingOlderToken = remoteKeyDao.remoteKeyForConversation(conversationId)?.olderToken
            RemoteKeyEntity(
              conversationId = conversationId,
              olderToken = existingOlderToken,
              newerToken = response.newerToken,
            )
          }

          LoadType.APPEND -> {
            val existingNewerToken = remoteKeyDao.remoteKeyForConversation(conversationId)?.newerToken
            RemoteKeyEntity(
              conversationId = conversationId,
              olderToken = response.olderToken,
              newerToken = existingNewerToken,
            )
          }
        }
        remoteKeyDao.insert(remoteKeyEntityToSave)
        chatDao.insertAll(response.messages.map { it.toChatMessageEntity(conversationId) })
      }
    }
    return MediatorResult.Success(
      endOfPaginationReached = when (loadType) {
        LoadType.REFRESH -> false
        LoadType.PREPEND -> response.messages.isEmpty()
        LoadType.APPEND -> response.olderToken == null
      },
    )
  }
}
