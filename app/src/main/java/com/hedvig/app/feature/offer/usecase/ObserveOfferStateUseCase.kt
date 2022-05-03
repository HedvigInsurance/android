package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

class ObserveOfferStateUseCase(
    private val offerRepository: OfferRepository
) {

    private val selectedVariantId = MutableStateFlow<String?>(null)

    fun observeOfferState(quoteCartId: QuoteCartId): Flow<Either<ErrorMessage, OfferState>> {
        return offerRepository
            .offerFlow
            .combine(selectedVariantId) { offer: Either<ErrorMessage, OfferModel>, variantId: String? ->
                offer.map { offerModel ->

                    val bundleVariant = offerModel.variants
                        .takeIf { variantId != null }
                        ?.find { it.id == variantId }
                        ?: offerModel.variants.first()

                    OfferState(offerModel, bundleVariant)
                }
            }.onStart {
                offerRepository.queryAndEmitOffer(quoteCartId)
            }
    }

    fun selectedVariant(variantId: String) {
        selectedVariantId.value = variantId
    }
}

data class OfferState(
    val offerModel: OfferModel,
    val selectedVariant: QuoteBundleVariant
) {
    val selectedQuoteIds = selectedVariant.bundle.quotes.map { it.id }

    fun findQuote(id: String) = selectedVariant.bundle.quotes.first { it.id == id }
}
