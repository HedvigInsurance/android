package com.hedvig.app.feature.genericauth

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateOtpAttemptMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e

interface CreateOtpAttemptUseCase {
  suspend fun invoke(email: String): CreateOtpResult
}

class CreateOtpAttemptUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateOtpAttemptUseCase {

  override suspend fun invoke(email: String) = when (
    val result = apolloClient
      .mutation(CreateOtpAttemptMutation(email = email))
      .safeQuery()
  ) {
    is QueryResult.Success -> CreateOtpResult.Success(result.data.login_createOtpAttempt)
    is QueryResult.Error -> {
      result.message?.let { e { it } }
      CreateOtpResult.Error
    }
  }
}

sealed interface CreateOtpResult {
  data class Success(val id: String) : CreateOtpResult
  object Error : CreateOtpResult
}
