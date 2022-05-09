package com.hedvig.app.feature.home.ui.changeaddress

import arrow.core.Either
import arrow.core.flatMap
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.app.feature.embark.QUOTE_CART_ID_KEY
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetAddressChangeStoryIdUseCase(
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
    private val apolloClient: ApolloClient,
) {

    suspend operator fun invoke(): SelfChangeEligibilityResult {
        return (
            apolloClient.query(ActiveContractBundlesQuery()).safeQuery().toEither().flatMap { data ->
                val storyId = data
                    .activeContractBundles
                    .firstOrNull()
                    ?.angelStories
                    ?.addressChangeV2
                if (storyId != null) {
                    addQuoteCartId(storyId).mapLeft { QueryResult.Error.GeneralError(it.message) }
                } else {
                    Either.Right(null)
                }
            }.fold(
                ifLeft = { SelfChangeEligibilityResult.Error(it.message) },
                ifRight = { maybeStoryId ->
                    if (maybeStoryId != null) {
                        SelfChangeEligibilityResult.Eligible(maybeStoryId)
                    } else {
                        SelfChangeEligibilityResult.Blocked
                    }
                }
            )
            )
    }

    private suspend fun addQuoteCartId(storyId: String): Either<ErrorMessage, String> {
        return createQuoteCartUseCase.invoke().map { quoteCartId ->
            appendQuoteCartId(storyId, quoteCartId.id)
        }
    }

    sealed class SelfChangeEligibilityResult {
        data class Eligible(val embarkStoryId: String) : SelfChangeEligibilityResult()
        object Blocked : SelfChangeEligibilityResult()
        data class Error(val message: String?) : SelfChangeEligibilityResult()
    }
}

fun appendQuoteCartId(embarkStoryId: String, quoteCartId: String) = if (embarkStoryId.contains("?")) {
    "$embarkStoryId&$QUOTE_CART_ID_KEY=$quoteCartId"
} else {
    "$embarkStoryId?$QUOTE_CART_ID_KEY=$quoteCartId"
}
