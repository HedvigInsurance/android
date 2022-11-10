package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.hedvig.app.feature.embark.util.SelectedContractType
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.SelectedVariantStore
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import slimber.log.e

class ObserveOfferStateUseCase(
  private val offerRepository: OfferRepository,
  private val selectedVariantStore: SelectedVariantStore,
) {

  fun invoke(
    quoteCartId: QuoteCartId,
    selectedContractTypes: List<SelectedContractType>,
  ): Flow<Either<ErrorMessage, OfferState>> = combine(
    offerRepository.offerFlow,
    selectedVariantStore.selectedVariantId,
  ) { offer: Either<ErrorMessage, OfferModel>, selectedVariantId: String? ->
    either {
      val offerModel = offer.bind()
      val bundleVariant = offerModel.getBundleVariant(selectedVariantId, selectedContractTypes)
      ensureNotNull(bundleVariant) {
        e { "bundleVariant was null for quote cart id:$quoteCartId and offer:$offerModel" }
        ErrorMessage()
      }
      OfferState(offerModel, bundleVariant)
    }
  }.onStart {
    offerRepository.fetchNewOffer(quoteCartId)
  }

  private fun OfferModel.getBundleVariant(
    selectedVariantId: String?,
    selectedContractTypes: List<SelectedContractType>,
  ): QuoteBundleVariant? {
    val bundleVariant: QuoteBundleVariant? = if (selectedVariantId != null) {
      variants.firstOrNull { it.id == selectedVariantId }
    } else {
      getPreselectedBundleVariant(selectedContractTypes)
    }
    return bundleVariant ?: variants.firstOrNull()
  }

  private fun OfferModel.getPreselectedBundleVariant(
    selectedContractTypes: List<SelectedContractType>,
  ): QuoteBundleVariant? {
    val selectedContractTypeIds = selectedContractTypes.map(SelectedContractType::id).toSet()
    return variants.firstOrNull { variant ->
      val insuranceTypesInBundle = variant.bundle.quotes.map(QuoteBundle.Quote::insuranceType).toSet()
      selectedContractTypeIds == insuranceTypesInBundle
    }
  }
}

data class OfferState(
  val offerModel: OfferModel,
  val selectedVariant: QuoteBundleVariant,
) {
  val selectedQuoteIds: List<String> = selectedVariant.bundle.quotes.map { it.id }

  fun findQuote(id: String): QuoteBundle.Quote? = selectedVariant.bundle.quotes.firstOrNull { it.id == id }
}
