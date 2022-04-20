package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class GetBundleVariantUseCase(
    offerRepository: OfferRepository
) {

    val selectedVariantId = MutableStateFlow("")
    val bundleVariantFlow: Flow<Either<ErrorMessage, Pair<OfferModel, QuoteBundleVariant>>> = offerRepository
        .offerFlow
        .combine(selectedVariantId) { offer: Either<ErrorMessage, OfferModel>, variantId: String ->
            offer.map { offerModel ->

                val bundleVariant = offerModel.variants
                    .takeIf { variantId.isNotEmpty() }
                    ?.find { it.id == variantId }
                    ?: offerModel.variants.first()

                Pair(offerModel, bundleVariant)
            }
        }

    fun invoke(variantId: String) {
        selectedVariantId.value = variantId
    }
}
