package com.hedvig.app.feature.genericauth.otpinput

import androidx.annotation.StringRes
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SendOtpCodeMutation
import com.hedvig.app.R
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SendOtpCodeUseCase(
    private val apolloClient: ApolloClient
) {
    suspend operator fun invoke(otpId: String, otpCode: String): OtpResult {
        return when (val result = apolloClient.mutate(SendOtpCodeMutation(otpId, otpCode)).safeQuery()) {
            is QueryResult.Error -> OtpResult.NetworkError(result.message)
            is QueryResult.Success -> parseSuccessResponse(result)
        }
    }

    private fun parseSuccessResponse(result: QueryResult.Success<SendOtpCodeMutation.Data>): OtpResult {
        val attempt = result.data.login_verifyOtpAttempt
        return attempt.asVerifyOtpLoginAttemptError?.let {
            val errorEvent = parseErrorCode(it.errorCode)
            OtpResult.OtpError(errorEvent)
        } ?: attempt.asVerifyOtpLoginAttemptSuccess?.let {
            OtpResult.Success(it.accessToken)
        } ?: OtpResult.OtpError(OtpErrorEvent.UNKNOWN)
    }

    private fun parseErrorCode(errorCode: String) = when (errorCode) {
        "TOO_MANY_ATTEMPTS" -> OtpErrorEvent.TOO_MANY_ATTEMPTS
        "ALREADY_COMPLETED" -> OtpErrorEvent.ALREADY_COMPLETED
        "EXPIRED" -> OtpErrorEvent.EXPIRED
        "WRONG_OTP" -> OtpErrorEvent.WRONG_OTP
        else -> OtpErrorEvent.UNKNOWN
    }

    sealed class OtpResult {
        data class Success(val authToken: String) : OtpResult()
        data class NetworkError(val message: String?) : OtpResult()
        data class OtpError(val error: OtpErrorEvent) : OtpResult()
    }

    enum class OtpErrorEvent(@StringRes private val resourceId: Int) : ErrorEvent {
        TOO_MANY_ATTEMPTS(R.string.login_code_input_error_msg_too_many_wrong_attempts),
        ALREADY_COMPLETED(R.string.login_code_input_error_msg_code_already_used),
        EXPIRED(R.string.login_code_input_error_msg_expired),
        WRONG_OTP(R.string.login_code_input_error_msg_code_not_valid),
        UNKNOWN(R.string.general_unknown_error);

        override fun getErrorResource() = resourceId
    }
}

interface ErrorEvent {
    @StringRes
    fun getErrorResource(): Int
}
