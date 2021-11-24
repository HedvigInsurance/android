package com.hedvig.app.feature.genericauth

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateOtpAttemptMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class CreateOtpAttemptUseCase(
    private val apolloClient: ApolloClient,
) {
    sealed class Result {
        data class Success(
            val token: String,
        ) : Result()

        object Error : Result()
    }

    suspend operator fun invoke(email: String) = when (
        val result = apolloClient
            .mutate(CreateOtpAttemptMutation(email = email))
            .safeQuery()
    ) {
        is QueryResult.Success -> {
            Result.Success(result.data.login_createOtpAttempt)
        }
        else -> Result.Error
    }
}
