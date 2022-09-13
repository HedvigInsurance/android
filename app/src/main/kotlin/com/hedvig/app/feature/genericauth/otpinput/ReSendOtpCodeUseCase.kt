package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.CreateOtpAttemptMutation
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute

interface ReSendOtpCodeUseCase {
  suspend operator fun invoke(credential: String): ResendOtpResult
}

class ReSendOtpCodeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : ReSendOtpCodeUseCase {
  override suspend operator fun invoke(credential: String): ResendOtpResult {
    return when (val result = apolloClient.mutation(CreateOtpAttemptMutation(credential)).safeExecute()) {
      is OperationResult.Error -> ResendOtpResult.Error(result.message)
      is OperationResult.Success -> ResendOtpResult.Success(result.toString())
    }
  }
}

sealed class ResendOtpResult {
  data class Success(val authToken: String) : ResendOtpResult()
  data class Error(val message: String?) : ResendOtpResult()
}
