package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SendOtpCodeMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SendOtpCodeUseCase(
    private val apolloClient: ApolloClient
) {
    suspend operator fun invoke(otpId: String, otpCode: String): OtpResult {
        return when (val result = apolloClient.mutate(SendOtpCodeMutation(otpId, otpCode)).safeQuery()) {
            is QueryResult.Error -> OtpResult.Error(result.message)
            is QueryResult.Success -> parseSuccessResponse(result)
        }
    }

    private fun parseSuccessResponse(result: QueryResult.Success<SendOtpCodeMutation.Data>): OtpResult {
        val attempt = result.data.login_verifyOtpAttempt
        return attempt.asVerifyOtpLoginAttemptError?.let {
            // TODO("Map error code")
            OtpResult.Error(it.errorCode)
        } ?: attempt.asVerifyOtpLoginAttemptSuccess?.let {
            OtpResult.Success(it.accessToken)
        } ?: OtpResult.Error(null)
    }

    sealed class OtpResult {
        data class Success(val authToken: String) : OtpResult()
        data class Error(val message: String?) : OtpResult()
    }
}
