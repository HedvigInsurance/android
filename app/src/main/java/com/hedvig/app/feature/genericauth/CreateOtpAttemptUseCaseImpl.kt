package com.hedvig.app.feature.genericauth

import arrow.core.continuations.either
import arrow.core.merge
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateOtpAttemptMutation
import com.hedvig.app.util.apollo.safeQuery
import e

interface CreateOtpAttemptUseCase {
  suspend fun invoke(email: String): CreateOtpResult
}

class CreateOtpAttemptUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateOtpAttemptUseCase {

  override suspend fun invoke(email: String): CreateOtpResult {
    return either {
      val result = apolloClient
        .mutation(CreateOtpAttemptMutation(email))
        .safeQuery()
        .toEither {
          if (it != null) e { it }
          CreateOtpResult.Error
        }
        .bind()
      CreateOtpResult.Success(result.login_createOtpAttempt)
    }.merge()
  }
}

sealed interface CreateOtpResult {
  data class Success(val id: String) : CreateOtpResult
  object Error : CreateOtpResult
}
