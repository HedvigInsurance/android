package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.QuoteCartAddCampaignMutation
import com.hedvig.android.owldroid.graphql.QuoteCartRemoveCampaignMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery

class EditCampaignUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository,
) {
    suspend fun addCampaignToQuoteCart(
        code: String,
        quoteCartId: QuoteCartId
    ): Either<ErrorMessage, QuoteCartId> = apolloClient
        .mutate(QuoteCartAddCampaignMutation(code, quoteCartId.id))
        .safeQuery()
        .toEither()
        .mapLeft { ErrorMessage(it.message) }
        .map { it.quoteCart_addCampaign }
        .flatMap {
            it.asQuoteCart
                ?.id
                ?.let { id -> QuoteCartId(id) }
                .rightIfNotNull { ErrorMessage(it.asBasicError?.message) }
        }
        .tap { offerRepository.queryAndEmitOffer(quoteCartId, emptyList()) }

    suspend fun removeCampaignFromQuoteCart(
        quoteCartId: QuoteCartId
    ): Either<ErrorMessage, QuoteCartId> = apolloClient
        .mutate(QuoteCartRemoveCampaignMutation(quoteCartId.id))
        .safeQuery()
        .toEither()
        .mapLeft { ErrorMessage(it.message) }
        .map { it.quoteCart_removeCampaign }
        .flatMap {
            it.asQuoteCart
                ?.id
                ?.let { id -> QuoteCartId(id) }
                .rightIfNotNull { ErrorMessage(it.asBasicError?.message) }
        }
        .tap { offerRepository.queryAndEmitOffer(quoteCartId, emptyList()) }
}
