package com.hedvig.android.feature.chat.data

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.model.CbmChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock

/**
 * Normally we'd add a fake impl here, but the database being involved makes this too much of a task for it to be worth
 * it. Best we just show errors everywhere instead.
 */
internal class CbmChatRepositoryDemo(
  private val clock: Clock,
) : CbmChatRepository {
  private val info = ConversationInfo.Info("1", null, clock.now(), true)
  private val demoErrorMessage = "No chat impl for demo"

  override suspend fun createConversation(conversationId: Uuid): Either<ErrorMessage, ConversationInfo.Info> {
    return info.right()
  }

  override fun getConversationInfo(conversationId: Uuid): Flow<Either<ApolloOperationError, ConversationInfo>> {
    return flowOf(info.right())
  }

  override fun bannerText(conversationId: Uuid): Flow<BannerText?> {
    return flowOf(null)
  }

  override suspend fun chatMessages(
    conversationId: Uuid,
    pagingToken: PagingToken?,
  ): Either<Throwable, ChatMessagePageResponse> {
    return Exception(demoErrorMessage).left()
  }

  override fun pollNewestMessages(conversationId: Uuid): Flow<String> {
    return flowOf()
  }

  override suspend fun retrySendMessage(conversationId: Uuid, messageId: String): Either<String, CbmChatMessage> {
    return demoErrorMessage.left()
  }

  override suspend fun sendText(conversationId: Uuid, messageId: Uuid?, text: String): Either<String, CbmChatMessage> {
    return demoErrorMessage.left()
  }

  override suspend fun sendPhotos(conversationId: Uuid, messageId: Uuid?, uriList: Uri): Either<String, CbmChatMessage> {
    return demoErrorMessage.left()
  }

  override suspend fun sendMedia(conversationId: Uuid, messageId: Uuid?, uriList: Uri): Either<String, CbmChatMessage> {
    return demoErrorMessage.left()
  }
}
