package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SendOtpCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.genericauth.otpinput.SendOtpCodeUseCase.OtpResult.Error.OtpError
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SendOtpCodeUseCase(
    private val apolloClient: ApolloClient
) {
    suspend operator fun invoke(otpId: String, otpCode: String): OtpResult {
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
        } ?: OtpError.Unknown
    }

    private fun parseErrorCode(errorCode: String) = when (errorCode) {
        "TOO_MANY_ATTEMPTS" -> OtpError.TooManyAttempts
        "ALREADY_COMPLETED" -> OtpError.AlreadyCompleted
        "EXPIRED" -> OtpError.Expired
        "WRONG_OTP" -> OtpError.WrongOtp
        else -> OtpError.Unknown
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
}

fun OtpError.toStringRes() = when (this) {
    OtpError.AlreadyCompleted -> R.string.login_code_input_error_msg_code_already_used
    OtpError.Expired -> R.string.login_code_input_error_msg_expired
    OtpError.TooManyAttempts -> R.string.login_code_input_error_msg_too_many_wrong_attempts
    OtpError.Unknown -> R.string.general_unknown_error
    OtpError.WrongOtp -> R.string.login_code_input_error_msg_code_not_valid
}
