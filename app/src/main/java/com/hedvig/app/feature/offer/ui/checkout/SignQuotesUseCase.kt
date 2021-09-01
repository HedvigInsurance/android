package com.hedvig.app.feature.offer.ui.checkout

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SignQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val tracker: OfferTracker,
) {

    sealed class SignQuoteResult {
        object Success : SignQuoteResult()
        data class StartSwedishBankId(
            val autoStartToken: String,
        ) : SignQuoteResult()

        data class Error(val message: String? = null) : SignQuoteResult()
    }

    suspend fun editAndSignQuotes(
        quoteIds: List<String>,
        ssn: String,
        email: String
    ): SignQuoteResult {
        val results = quoteIds.map { quoteId ->
            val mutation = EditMailAndSSNMutation(quoteId, ssn, email)
            apolloClient.mutate(mutation).safeQuery()
        }

        return if (results.all { it is QueryResult.Success }) {
            val result = signQuotes(quoteIds)
            if (result == SignQuoteResult.Success) {
                tracker.signQuotes(quoteIds)
            }
            result
        } else {
            results.firstOrNull { it is QueryResult.Error }
                ?.let { SignQuoteResult.Error((it as? QueryResult.Error)?.message) }
                ?: SignQuoteResult.Error()
        }
    }

    suspend fun signQuotes(quoteIds: List<String>): SignQuoteResult {
        val mutation = SignQuotesMutation(quoteIds)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> {
                val signResponse = result.data?.signOrApproveQuotes?.asSignQuoteResponse?.signResponse
                signResponse?.asFailedToStartSign?.let {
                    SignQuoteResult.Error(it.errorMessage)
                } ?: signResponse?.asSimpleSignSession?.let {
                    SignQuoteResult.Success
                } ?: signResponse?.asSwedishBankIdSession?.let {
                    val autoStartToken = it.autoStartToken ?: return@let SignQuoteResult.Error()
                    SignQuoteResult.StartSwedishBankId(autoStartToken)
                } ?: SignQuoteResult.Error(null)
            }
        }
    }
}
