package com.hedvig.app.feature.offer.ui.checkout

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ApproveQuotesMutation
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SignQuotesUseCase(private val apolloClient: ApolloClient) {

    sealed class SignQuoteResult {
        object Success : SignQuoteResult()
        data class Error(val message: String?) : SignQuoteResult()
    }

    suspend fun editAndSignQuotes(
        quoteIds: List<String>,
        ssn: String,
        email: String
    ): SignQuoteResult {
        val mutation = EditMailAndSSNMutation(ssn, email)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> signQuotes(quoteIds)
        }
    }

    suspend fun signQuotes(quoteIds: List<String>): SignQuoteResult {
        val mutation = SignQuotesMutation(quoteIds)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> {
                result.data.signQuotes.asSimpleSignSession?.let {
                    SignQuoteResult.Success
                } ?: result.data.signQuotes.asFailedToStartSign?.let {
                    SignQuoteResult.Error(it.errorMessage)
                } ?: SignQuoteResult.Error(null)
            }
        }
    }

    suspend fun approveQuotes(quoteIds: List<String>): SignQuoteResult {
        val mutation = ApproveQuotesMutation(quoteIds)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> SignQuoteResult.Success
        }
    }
}
