package com.hedvig.app.feature.home.ui.changeaddress

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import arrow.core.identity
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery
import com.hedvig.app.feature.embark.QUOTE_CART_ID_KEY
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.util.apollo.safeQuery

class GetAddressChangeStoryIdUseCase(
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
    private val apolloClient: ApolloClient,
) {

    suspend fun invoke(): SelfChangeEligibilityResult {
        return either<SelfChangeFailureType, SelfChangeEligibilityResult.Eligible> {
            val activeContractBundlesQueryData = apolloClient.query(ActiveContractBundlesQuery())
                .safeQuery()
                .toEither(SelfChangeFailureType::Error)
                .bind()
            val storyId =
                activeContractBundlesQueryData.activeContractBundles.firstOrNull()?.angelStories?.addressChangeV2
            ensureNotNull(storyId) { SelfChangeFailureType.Blocked }
            val storyIdWithQuoteCartId = addQuoteCartId(storyId).bind()
            SelfChangeEligibilityResult.Eligible(storyIdWithQuoteCartId)
        }.fold(SelfChangeFailureType::toSelfChangeEligibilityResult, ::identity)
    }

    private suspend fun addQuoteCartId(storyId: String): Either<SelfChangeFailureType.Error, String> {
        return createQuoteCartUseCase.invoke()
            .bimap(
                { errorMessage -> SelfChangeFailureType.Error(errorMessage.message) },
                { quoteCartId -> appendQuoteCartId(storyId, quoteCartId.id) }
            )
    }

    sealed class SelfChangeEligibilityResult {
        data class Eligible(val embarkStoryId: String) : SelfChangeEligibilityResult()
        object Blocked : SelfChangeEligibilityResult()
        data class Error(val message: String?) : SelfChangeEligibilityResult()
    }

    private sealed interface SelfChangeFailureType {
        fun toSelfChangeEligibilityResult(): SelfChangeEligibilityResult {
            return when (this) {
                Blocked -> SelfChangeEligibilityResult.Blocked
                is Error -> SelfChangeEligibilityResult.Error(this.message)
            }
        }

        object Blocked : SelfChangeFailureType

        data class Error(val message: String?) : SelfChangeFailureType
    }
}

fun appendQuoteCartId(embarkStoryId: String, quoteCartId: String) = if (embarkStoryId.contains("?")) {
    "$embarkStoryId&$QUOTE_CART_ID_KEY=$quoteCartId"
} else {
    "$embarkStoryId?$QUOTE_CART_ID_KEY=$quoteCartId"
}
