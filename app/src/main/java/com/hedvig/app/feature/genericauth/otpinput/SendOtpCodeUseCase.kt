package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SendOtpCodeMutation
import com.hedvig.app.R
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

interface SendOtpCodeUseCase {
    suspend operator fun invoke(otpId: String, otpCode: String): OtpResult
}

class SendOtpCodeUseCaseImpl(
    private val apolloClient: ApolloClient
) : SendOtpCodeUseCase {
    override suspend operator fun invoke(otpId: String, otpCode: String): OtpResult {
        return when (val result = apolloClient.mutate(SendOtpCodeMutation(otpId, otpCode)).safeQuery()) {
            is QueryResult.Error -> OtpResult.Error.NetworkError(result.message)
            is QueryResult.Success -> parseSuccessResponse(result)
        }
    }

    private fun parseSuccessResponse(result: QueryResult.Success<SendOtpCodeMutation.Data>): OtpResult {
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

fun OtpResult.Error.OtpError.toStringRes() = when (this) {
    OtpResult.Error.OtpError.AlreadyCompleted -> R.string.login_code_input_error_msg_code_already_used
    OtpResult.Error.OtpError.Expired -> R.string.login_code_input_error_msg_expired
    OtpResult.Error.OtpError.TooManyAttempts -> R.string.login_code_input_error_msg_too_many_wrong_attempts
    OtpResult.Error.OtpError.Unknown -> R.string.general_unknown_error
    OtpResult.Error.OtpError.WrongOtp -> R.string.login_code_input_error_msg_code_not_valid
}
