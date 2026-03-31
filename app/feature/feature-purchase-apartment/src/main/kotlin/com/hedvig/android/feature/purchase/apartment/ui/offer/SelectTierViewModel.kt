package com.hedvig.android.feature.purchase.apartment.ui.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.apartment.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.TierOfferData
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectTierViewModel(
  params: SelectTierParameters,
) : MoleculeViewModel<SelectTierEvent, SelectTierUiState>(
    buildInitialState(params),
    SelectTierPresenter(params),
  )

private fun buildInitialState(params: SelectTierParameters): SelectTierUiState {
  val tierGroups = groupOffersByTier(params.offers)
  val defaultTierName = tierGroups.firstOrNull { "Standard" in it.tierDisplayName }?.tierDisplayName
    ?: tierGroups.firstOrNull()?.tierDisplayName
    ?: ""
  val defaultDeductibleByTier = tierGroups.associate { group ->
    group.tierDisplayName to (group.deductibleOptions.minByOrNull { it.netAmount }?.offerId ?: "")
  }
  return SelectTierUiState(
    tierGroups = tierGroups,
    selectedTierName = defaultTierName,
    selectedDeductibleByTier = defaultDeductibleByTier,
    shopSessionId = params.shopSessionId,
    productDisplayName = params.productDisplayName,
    summaryToNavigate = null,
  )
}

private fun groupOffersByTier(offers: List<TierOfferData>): List<TierGroup> {
  return offers.groupBy { it.tierDisplayName }.map { (tierName, tierOffers) ->
    val first = tierOffers.first()
    TierGroup(
      tierDisplayName = tierName,
      tierDescription = first.tierDescription,
      usps = first.usps,
      deductibleOptions = tierOffers.map { offer ->
        DeductibleOption(
          offerId = offer.offerId,
          deductibleDisplayName = offer.deductibleDisplayName ?: "",
          netAmount = offer.netAmount,
          netCurrencyCode = offer.netCurrencyCode,
          grossAmount = offer.grossAmount,
          grossCurrencyCode = offer.grossCurrencyCode,
          hasDiscount = offer.hasDiscount,
        )
      }.sortedBy { it.netAmount },
    )
  }
}

internal class SelectTierPresenter(
  private val params: SelectTierParameters,
) : MoleculePresenter<SelectTierEvent, SelectTierUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectTierEvent>.present(lastState: SelectTierUiState): SelectTierUiState {
    var selectedTierName by remember { mutableStateOf(lastState.selectedTierName) }
    var selectedDeductibleByTier by remember { mutableStateOf(lastState.selectedDeductibleByTier) }
    var summaryToNavigate: SummaryParameters? by remember { mutableStateOf(lastState.summaryToNavigate) }

    CollectEvents { event ->
      when (event) {
        is SelectTierEvent.SelectTier -> {
          selectedTierName = event.tierName
        }

        is SelectTierEvent.SelectDeductible -> {
          selectedDeductibleByTier = selectedDeductibleByTier + (event.tierName to event.offerId)
        }

        SelectTierEvent.Continue -> {
          val selectedOfferId = selectedDeductibleByTier[selectedTierName] ?: return@CollectEvents
          val selectedOffer = params.offers.first { it.offerId == selectedOfferId }
          summaryToNavigate = SummaryParameters(
            shopSessionId = params.shopSessionId,
            selectedOffer = selectedOffer,
            productDisplayName = params.productDisplayName,
          )
        }

        SelectTierEvent.ClearNavigation -> {
          summaryToNavigate = null
        }
      }
    }

    return SelectTierUiState(
      tierGroups = lastState.tierGroups,
      selectedTierName = selectedTierName,
      selectedDeductibleByTier = selectedDeductibleByTier,
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = summaryToNavigate,
    )
  }
}

internal data class TierGroup(
  val tierDisplayName: String,
  val tierDescription: String,
  val usps: List<String>,
  val deductibleOptions: List<DeductibleOption>,
)

internal data class DeductibleOption(
  val offerId: String,
  val deductibleDisplayName: String,
  val netAmount: Double,
  val netCurrencyCode: String,
  val grossAmount: Double,
  val grossCurrencyCode: String,
  val hasDiscount: Boolean,
)

internal data class SelectTierUiState(
  val tierGroups: List<TierGroup>,
  val selectedTierName: String,
  val selectedDeductibleByTier: Map<String, String>,
  val shopSessionId: String,
  val productDisplayName: String,
  val summaryToNavigate: SummaryParameters?,
)

internal sealed interface SelectTierEvent {
  data class SelectTier(val tierName: String) : SelectTierEvent

  data class SelectDeductible(val tierName: String, val offerId: String) : SelectTierEvent

  data object Continue : SelectTierEvent

  data object ClearNavigation : SelectTierEvent
}
