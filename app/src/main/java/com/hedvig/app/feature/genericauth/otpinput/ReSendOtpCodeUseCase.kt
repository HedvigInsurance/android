package com.hedvig.app.feature.genericauth.otpinput

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateOtpAttemptMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class ReSendOtpCodeUseCase(
    private val apolloClient: ApolloClient
) {
    suspend operator fun invoke(credential: String): ResendOtpResult {
        return when (val result = apolloClient.mutate(CreateOtpAttemptMutation(credential)).safeQuery()) {
            is QueryResult.Error -> ResendOtpResult.Error(result.message)
            is QueryResult.Success -> ResendOtpResult.Success(result.toString())
        }
    }

    sealed class ResendOtpResult {
        data class Success(val authToken: String) : ResendOtpResult()
        data class Error(val message: String?) : ResendOtpResult()
    }
}
