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

  suspend fun sendFile(uri: Uri): Either<ErrorMessage, ChatMessage>

  suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessage>
}
