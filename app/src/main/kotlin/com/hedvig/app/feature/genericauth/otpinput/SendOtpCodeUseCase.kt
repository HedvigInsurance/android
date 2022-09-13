package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.SendOtpCodeMutation
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute

interface SendOtpCodeUseCase {
  suspend operator fun invoke(otpId: String, otpCode: String): OtpResult
}

class SendOtpCodeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SendOtpCodeUseCase {
  override suspend operator fun invoke(otpId: String, otpCode: String): OtpResult {
    return when (val result = apolloClient.mutation(SendOtpCodeMutation(otpId, otpCode)).safeExecute()) {
      is OperationResult.Error -> OtpResult.Error.NetworkError(result.message)
      is OperationResult.Success -> parseSuccessResponse(result)
    }
  }

  private fun parseSuccessResponse(result: OperationResult.Success<SendOtpCodeMutation.Data>): OtpResult {
    val attempt = result.data.login_verifyOtpAttempt
    return attempt.asVerifyOtpLoginAttemptError?.let {
      parseErrorCode(it.errorCode)
    } ?: attempt.asVerifyOtpLoginAttemptSuccess?.let {
      OtpResult.Success(it.accessToken)
    } ?: OtpResult.Error.OtpError.Unknown
  }

  private fun parseErrorCode(errorCode: String) = when (errorCode) {
    "TOO_MANY_ATTEMPTS" -> OtpResult.Error.OtpError.TooManyAttempts
    "ALREADY_COMPLETED" -> OtpResult.Error.OtpError.AlreadyCompleted
    "EXPIRED" -> OtpResult.Error.OtpError.Expired
    "WRONG_OTP" -> OtpResult.Error.OtpError.WrongOtp
    else -> OtpResult.Error.OtpError.Unknown
  }
}

sealed class OtpResult {
  data class Success(val authToken: String) : OtpResult()

  sealed class Error : OtpResult() {
    data class NetworkError(val message: String?) : Error()
    sealed class OtpError : Error() {
      object TooManyAttempts : OtpError()
      object AlreadyCompleted : OtpError()
      object Expired : OtpError()
      object WrongOtp : OtpError()
      object Unknown : OtpError()
    }
  }
}
