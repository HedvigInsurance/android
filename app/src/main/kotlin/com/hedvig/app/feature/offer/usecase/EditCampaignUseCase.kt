package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.QuoteCartAddCampaignMutation
import com.hedvig.android.apollo.graphql.QuoteCartRemoveCampaignMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither

@JvmInline
value class CampaignCode(val code: String)

class EditCampaignUseCase(
  private val apolloClient: ApolloClient,
  private val offerRepository: OfferRepository,
) {
  suspend fun addCampaignToQuoteCart(
    campaignCode: CampaignCode,
    quoteCartId: QuoteCartId,
  ): Either<ErrorMessage, QuoteCartId> = apolloClient
    .mutation(QuoteCartAddCampaignMutation(campaignCode.code, quoteCartId.id))
    .safeQuery()
    .toEither(::ErrorMessage)
    .map { it.quoteCart_addCampaign }
    .flatMap {
      it.asQuoteCart
        ?.id
        ?.let { id -> QuoteCartId(id) }
        .rightIfNotNull { ErrorMessage(it.asBasicError?.message) }
    }
    .tap { offerRepository.queryAndEmitOffer(quoteCartId) }

  suspend fun removeCampaignFromQuoteCart(
    quoteCartId: QuoteCartId,
  ): Either<ErrorMessage, QuoteCartId> = apolloClient
    .mutation(QuoteCartRemoveCampaignMutation(quoteCartId.id))
    .safeQuery()
    .toEither(::ErrorMessage)
    .map { it.quoteCart_removeCampaign }
    .flatMap {
      it.asQuoteCart
        ?.id
        ?.let { id -> QuoteCartId(id) }
        .rightIfNotNull { ErrorMessage(it.asBasicError?.message) }
    }
    .tap { offerRepository.queryAndEmitOffer(quoteCartId) }
}
