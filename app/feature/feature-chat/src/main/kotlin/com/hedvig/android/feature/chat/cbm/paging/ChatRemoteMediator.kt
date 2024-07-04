package com.hedvig.android.feature.chat.cbm.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import arrow.core.getOrElse
import com.benasher44.uuid.Uuid
import com.hedvig.android.feature.chat.cbm.CbmChatRepository
import com.hedvig.android.feature.chat.cbm.PagingToken
import com.hedvig.android.feature.chat.cbm.database.AppDatabase
import com.hedvig.android.feature.chat.cbm.database.ChatDao
import com.hedvig.android.feature.chat.cbm.database.ChatMessageEntity
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyEntity
import com.hedvig.android.feature.chat.cbm.model.toChatMessageEntity

@OptIn(ExperimentalPagingApi::class)
internal class ChatRemoteMediator(
  private val conversationId: Uuid,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val chatRepository: CbmChatRepository,
) : RemoteMediator<Int, ChatMessageEntity>() {
  override suspend fun load(loadType: LoadType, state: PagingState<Int, ChatMessageEntity>): MediatorResult {
    println("Stelios@@ loadType:$loadType")
    val pagingToken = when (loadType) {
      LoadType.REFRESH -> null
      LoadType.PREPEND -> {
        val newerToken = remoteKeyDao.remoteKeyForConversation(conversationId).newerToken
        newerToken ?: return MediatorResult.Success(endOfPaginationReached = true)
        PagingToken.NewerToken(newerToken)
      }
      LoadType.APPEND -> {
        val olderToken = remoteKeyDao.remoteKeyForConversation(conversationId).olderToken
        olderToken ?: return MediatorResult.Success(endOfPaginationReached = true)
        PagingToken.OlderToken(olderToken)
      }
    }
    val response = chatRepository.chatMessages(conversationId, pagingToken).getOrElse {
      return MediatorResult.Error(Exception(it))
    }
    val isRefreshingDueToJumping = loadType == LoadType.REFRESH && state.pages.isNotEmpty()
    if (isRefreshingDueToJumping) {
      database.withTransaction {
        val existingOlderToken = remoteKeyDao.remoteKeyForConversation(conversationId).olderToken
        remoteKeyDao.insert(RemoteKeyEntity(conversationId, existingOlderToken, response.newerToken!!))
        chatDao.insertAll(response.messages.map { it.toChatMessageEntity(conversationId) })
      }
    } else {
      database.withTransaction {
        if (loadType == LoadType.REFRESH) {
          chatDao.clearRemoteMessages(conversationId)
          remoteKeyDao.deleteAllForConversation(conversationId)
        }
        val remoteKeyEntityToSave = when (loadType) {
          LoadType.REFRESH -> RemoteKeyEntity(conversationId, response.olderToken, response.newerToken!!)
          LoadType.PREPEND -> {
            val existingOlderToken = remoteKeyDao.remoteKeyForConversation(conversationId).olderToken
            RemoteKeyEntity(
              conversationId = conversationId,
              olderToken = existingOlderToken,
              newerToken = response.newerToken,
            )
          }
          LoadType.APPEND -> {
            val existingNewerToken = remoteKeyDao.remoteKeyForConversation(conversationId).newerToken
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
        LoadType.PREPEND -> response.messages.isEmpty() // backend never returns null "newerToken"
        LoadType.APPEND -> response.olderToken == null
      },
    )
  }
}
