package com.hedvig.app.feature.offer.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.QuoteCartStartCheckoutMutation
import com.hedvig.android.owldroid.graphql.SignQuotesMutation
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager

class SignQuotesUseCase(
    private val apolloClient: ApolloClient,
    private val cacheManager: CacheManager,
    private val featureManager: FeatureManager,
    private val localeManager: LocaleManager,
) {

    sealed class SignQuoteResult {
        object Success : SignQuoteResult()
        data class StartSwedishBankId(
            val autoStartToken: String,
        ) : SignQuoteResult()

        data class Error(val message: String? = null) : SignQuoteResult()
    }

    suspend fun signQuotesAndClearCache(quoteIds: List<String>, quoteCartId: String?): SignQuoteResult {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            signQuoteCart(quoteIds, quoteCartId)
        } else {
            signQuotes(quoteIds)
        }
    }

    private suspend fun signQuoteCart(quoteIds: List<String>, quoteCartId: String?): SignQuoteResult {
        return if (quoteCartId == null) {
            SignQuoteResult.Error(null)
        } else {
            val mutation = QuoteCartStartCheckoutMutation(quoteCartId, quoteIds, localeManager.defaultLocale())
            when (val result = apolloClient.mutate(mutation).safeQuery()) {
                is QueryResult.Error -> SignQuoteResult.Error(result.message)
                is QueryResult.Success -> {
                    result.data.quoteCartStartCheckout.asBasicError?.let {
                        SignQuoteResult.Error(it.message)
                    } ?: result.data.quoteCartStartCheckout.asQuoteCart?.paymentConnection?.id?.let {
                        SignQuoteResult.StartSwedishBankId(it)
                    } ?: SignQuoteResult.Error(null)
                }
            }
        }
    }

    private suspend fun signQuotes(quoteIds: List<String>): SignQuoteResult {
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
