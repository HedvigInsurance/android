package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.QuoteCartAddCampaignMutation
import com.hedvig.android.apollo.graphql.QuoteCartRemoveCampaignMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage

@JvmInline
value class CampaignCode(val code: String)

class EditCampaignUseCase(
  private val apolloClient: ApolloClient,
  private val offerRepository: OfferRepository,
) {
  suspend fun addCampaignToQuoteCart(
    campaignCode: CampaignCode,
    quoteCartId: QuoteCartId,
  ): Either<ErrorMessage, QuoteCartId> {
    return either {
      val addCampaignResponse = apolloClient
        .mutation(QuoteCartAddCampaignMutation(campaignCode.code, quoteCartId.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .map { it.quoteCart_addCampaign }
        .bind()

      val quoteCartIdResponse: QuoteCartId? = addCampaignResponse.asQuoteCart?.id?.let(::QuoteCartId)
      ensureNotNull(quoteCartIdResponse) {
        ErrorMessage((addCampaignResponse.asBasicError?.message))
      }
      offerRepository.fetchNewOffer(quoteCartIdResponse)
      quoteCartIdResponse
    }
  }

  suspend fun removeCampaignFromQuoteCart(
    quoteCartId: QuoteCartId,
  ): Either<ErrorMessage, QuoteCartId> {
    return either {
      val editCampaignResponse = apolloClient
        .mutation(QuoteCartRemoveCampaignMutation(quoteCartId.id))
        .safeExecute()
        .toEither(::ErrorMessage)
        .map { it.quoteCart_removeCampaign }
        .bind()

      val quoteCartIdResponse: QuoteCartId? = editCampaignResponse.asQuoteCart?.id?.let(::QuoteCartId)
      ensureNotNull(quoteCartIdResponse) {
        ErrorMessage(editCampaignResponse.asBasicError?.message)
      }
      offerRepository.fetchNewOffer(quoteCartIdResponse)
      quoteCartIdResponse
    }
  }
}
