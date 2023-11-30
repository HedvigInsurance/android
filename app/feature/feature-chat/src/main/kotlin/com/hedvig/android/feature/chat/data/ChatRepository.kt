package com.hedvig.android.feature.chat.data

import android.net.Uri
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.feature.chat.model.ChatMessagesResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ChatRepository {
  suspend fun fetchMoreMessages(until: Instant): Either<ErrorMessage, ChatMessagesResult>

  suspend fun pollNewestMessages(): Either<ErrorMessage, ChatMessagesResult>

  suspend fun watchMessages(): Flow<Either<ErrorMessage, List<ChatMessage>>>

  /**
   * Photo URI received from using [com.hedvig.android.compose.photo.capture.state.PhotoCaptureState]
   */
  suspend fun sendPhoto(uri: Uri): Either<ErrorMessage, ChatMessage>

  /**
   * Media URI, receved from [androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia]
   * e.g. content://media/picker/0/com.android.providers.media.photopicker/media/1000003268
   */
  suspend fun sendMedia(uri: Uri): Either<ErrorMessage, ChatMessage>

  suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessage>
}
