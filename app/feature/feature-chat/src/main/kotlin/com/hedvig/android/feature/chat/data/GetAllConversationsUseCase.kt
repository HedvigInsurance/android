package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal interface GetAllConversationsUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>>
}

internal class GetAllConversationsUseCaseImpl(apolloClient: ApolloClient) : GetAllConversationsUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, List<Conversation>>> {
    // todo! remove mock with real impl
    return flow {
      either<ErrorMessage, List<Conversation>> {
        listOf()
      }
    }
  }
}
