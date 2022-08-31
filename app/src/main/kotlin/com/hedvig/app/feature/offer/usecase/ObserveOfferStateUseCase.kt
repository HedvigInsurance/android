package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import com.hedvig.app.feature.embark.util.SelectedContractType
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
  private val offerRepository: OfferRepository,
) {

  private val selectedVariantId = MutableStateFlow<String?>(null)

  fun observeOfferState(
    quoteCartId: QuoteCartId,
    selectedContractTypes: List<SelectedContractType>,
  ): Flow<Either<ErrorMessage, OfferState>> = offerRepository
    .offerFlow
    .combine(selectedVariantId) { offer: Either<ErrorMessage, OfferModel>, selectedVariantId: String? ->
      offer.map { offerModel ->
        val bundleVariant = offerModel.getBundleVariant(selectedVariantId, selectedContractTypes)
        OfferState(offerModel, bundleVariant)
      }
    }.onStart {
      offerRepository.queryAndEmitOffer(quoteCartId)
    }

  private fun OfferModel.getBundleVariant(
    selectedVariantId: String?,
    selectedContractTypes: List<SelectedContractType>,
  ): QuoteBundleVariant {
    val bundleVariant = if (selectedVariantId != null) {
      variants.find { it.id == selectedVariantId }
    } else {
      getPreselectedBundleVariant(selectedContractTypes)
    }
    return bundleVariant ?: variants.first()
  }

  private fun OfferModel.getPreselectedBundleVariant(
    selectedContractTypes: List<SelectedContractType>,
  ) = variants.find {
    val insuranceTypesInBundle = it.bundle.quotes.map { it.insuranceType }.toSet()
    val selectedContractTypeIds = selectedContractTypes.map { it.id }.toSet()
    selectedContractTypeIds == insuranceTypesInBundle
  }

  fun selectedVariant(variantId: String) {
    selectedVariantId.value = variantId
  }
}

data class OfferState(
  val offerModel: OfferModel,
  val selectedVariant: QuoteBundleVariant,
) {
  val selectedQuoteIds = selectedVariant.bundle.quotes.map { it.id }

  fun findQuote(id: String) = selectedVariant.bundle.quotes.first { it.id == id }
}
