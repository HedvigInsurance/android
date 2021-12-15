package com.hedvig.app.feature.offer.ui.checkout

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EditMailAndSSNMutation
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class SignQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val tracker: OfferTracker,
    private val cacheManager: CacheManager
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
        val results = quoteIds.map {
            editAndSignQuote(it, ssn, email)
        }

        return if (results.all { it is SignQuoteResult.Success }) {
            val result = signQuotesAndClearCache(quoteIds)
            if (result == SignQuoteResult.Success) {
                tracker.signQuotes()
            }
            result
        } else {
            results.firstOrNull { it is SignQuoteResult.Error }
                ?.let { SignQuoteResult.Error((it as? SignQuoteResult.Error)?.message) }
                ?: SignQuoteResult.Error()
        }
    }

    private suspend fun editAndSignQuote(
        quoteId: String,
        ssn: String,
        email: String
    ): SignQuoteResult {
        val mutation = EditMailAndSSNMutation(quoteId, ssn, email)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> checkUnderwriterLimits(result)
        }
    }

    private fun checkUnderwriterLimits(result: QueryResult.Success<EditMailAndSSNMutation.Data>) =
        if (result.data.editQuote.asUnderwritingLimitsHit != null) {
            val codes = result.data.editQuote.asUnderwritingLimitsHit?.limits?.joinToString { it.code }
            SignQuoteResult.Error(codes)
        } else {
            SignQuoteResult.Success
        }

    suspend fun signQuotesAndClearCache(quoteIds: List<String>): SignQuoteResult {
        val mutation = SignQuotesMutation(quoteIds)
        return when (val result = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Error -> SignQuoteResult.Error(result.message)
            is QueryResult.Success -> {
                val signResponse = result.data?.signOrApproveQuotes?.asSignQuoteResponse?.signResponse
                signResponse?.asFailedToStartSign?.let {
                    SignQuoteResult.Error(it.errorMessage)
                } ?: signResponse?.asSimpleSignSession?.let {
                    cacheManager.clearCache()
                    SignQuoteResult.Success
                } ?: signResponse?.asSwedishBankIdSession?.let {
                    val autoStartToken = it.autoStartToken ?: return@let SignQuoteResult.Error()
                    SignQuoteResult.StartSwedishBankId(autoStartToken)
                } ?: SignQuoteResult.Error(null)
            }
        }
    }
}
