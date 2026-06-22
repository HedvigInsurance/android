package com.hedvig.android.feature.chat.data

import android.net.Uri
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingCbmChatRepository(
  private val demoManager: DemoManager,
  private val prodImpl: CbmChatRepositoryImpl,
  private val demoImpl: CbmChatRepositoryDemo,
) : CbmChatRepository {
  override suspend fun createConversation(conversationId: Uuid) = pick().createConversation(conversationId)

  override fun getConversationInfo(conversationId: Uuid) = flow {
    emitAll(pick().getConversationInfo(conversationId))
  }

  override fun bannerText(conversationId: Uuid) = flow {
    emitAll(pick().bannerText(conversationId))
  }

  override suspend fun chatMessages(conversationId: Uuid, pagingToken: PagingToken?) =
    pick().chatMessages(conversationId, pagingToken)

  override fun pollNewestMessages(conversationId: Uuid) = flow {
    emitAll(pick().pollNewestMessages(conversationId))
  }

  override suspend fun retrySendMessage(conversationId: Uuid, messageId: String) =
    pick().retrySendMessage(conversationId, messageId)

  override suspend fun sendText(conversationId: Uuid, retryingMessageId: Uuid?, text: String) =
    pick().sendText(conversationId, retryingMessageId, text)

  override suspend fun sendPhotos(conversationId: Uuid, uriList: List<Uri>) = pick().sendPhotos(conversationId, uriList)

  override suspend fun sendMedia(conversationId: Uuid, uriList: List<Uri>) = pick().sendMedia(conversationId, uriList)

  private suspend fun pick(): CbmChatRepository = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
