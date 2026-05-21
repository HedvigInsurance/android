package com.hedvig.android.feature.purchase.common.ui.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

class SelectTierViewModel(
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
    dialogTierName = defaultTierName.takeIf { it.isNotEmpty() },
    dialogDeductibleId = defaultDeductibleByTier[defaultTierName],
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

class SelectTierPresenter(
  private val params: SelectTierParameters,
) : MoleculePresenter<SelectTierEvent, SelectTierUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectTierEvent>.present(lastState: SelectTierUiState): SelectTierUiState {
    var selectedTierName by remember { mutableStateOf(lastState.selectedTierName) }
    var selectedDeductibleByTier by remember { mutableStateOf(lastState.selectedDeductibleByTier) }
    var dialogTierName: String? by remember { mutableStateOf(lastState.dialogTierName) }
    var dialogDeductibleId: String? by remember { mutableStateOf(lastState.dialogDeductibleId) }
    var summaryToNavigate: SummaryParameters? by remember { mutableStateOf(lastState.summaryToNavigate) }

    CollectEvents { event ->
      when (event) {
        is SelectTierEvent.SelectTierInDialog -> {
          dialogTierName = event.tierName
        }

        is SelectTierEvent.SelectDeductibleInDialog -> {
          dialogDeductibleId = event.offerId
        }

        SelectTierEvent.ConfirmTier -> {
          val newTierName = dialogTierName ?: return@CollectEvents
          selectedTierName = newTierName
          val group = lastState.tierGroups.firstOrNull { it.tierDisplayName == newTierName }
          if (group != null) {
            val cheapest = group.deductibleOptions.minByOrNull { it.netAmount }
            if (cheapest != null) {
              selectedDeductibleByTier = selectedDeductibleByTier + (newTierName to cheapest.offerId)
              dialogDeductibleId = cheapest.offerId
            }
          }
        }

        SelectTierEvent.ConfirmDeductible -> {
          val newDeductibleId = dialogDeductibleId ?: return@CollectEvents
          selectedDeductibleByTier = selectedDeductibleByTier + (selectedTierName to newDeductibleId)
        }

        SelectTierEvent.RevertTierToConfirmed -> {
          dialogTierName = selectedTierName.takeIf { it.isNotEmpty() }
        }

        SelectTierEvent.RevertDeductibleToConfirmed -> {
          dialogDeductibleId = selectedDeductibleByTier[selectedTierName]
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
      dialogTierName = dialogTierName,
      dialogDeductibleId = dialogDeductibleId,
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = summaryToNavigate,
    )
  }
}

data class TierGroup(
  val tierDisplayName: String,
  val tierDescription: String,
  val usps: List<String>,
  val deductibleOptions: List<DeductibleOption>,
)

data class DeductibleOption(
  val offerId: String,
  val deductibleDisplayName: String,
  val netAmount: Double,
  val netCurrencyCode: String,
  val grossAmount: Double,
  val grossCurrencyCode: String,
  val hasDiscount: Boolean,
)

data class SelectTierUiState(
  val tierGroups: List<TierGroup>,
  val selectedTierName: String,
  val selectedDeductibleByTier: Map<String, String>,
  val dialogTierName: String?,
  val dialogDeductibleId: String?,
  val shopSessionId: String,
  val productDisplayName: String,
  val summaryToNavigate: SummaryParameters?,
) {
  val selectedTierIndex: Int?
    get() = tierGroups.indexOfFirst { it.tierDisplayName == selectedTierName }.takeIf { it >= 0 }

  val selectedDeductibleIndex: Int?
    get() {
      val group = tierGroups.firstOrNull { it.tierDisplayName == selectedTierName } ?: return null
      val deductibleId = selectedDeductibleByTier[selectedTierName] ?: return null
      return group.deductibleOptions.indexOfFirst { it.offerId == deductibleId }.takeIf { it >= 0 }
    }

  val currentDeductibleOptions: List<DeductibleOption>
    get() = tierGroups.firstOrNull { it.tierDisplayName == selectedTierName }?.deductibleOptions ?: emptyList()

  val selectedDeductible: DeductibleOption?
    get() {
      val deductibleId = selectedDeductibleByTier[selectedTierName] ?: return null
      return currentDeductibleOptions.firstOrNull { it.offerId == deductibleId }
    }
}

sealed interface SelectTierEvent {
  data class SelectTierInDialog(val tierName: String) : SelectTierEvent

  data class SelectDeductibleInDialog(val offerId: String) : SelectTierEvent

  data object ConfirmTier : SelectTierEvent

  data object ConfirmDeductible : SelectTierEvent

  data object RevertTierToConfirmed : SelectTierEvent

  data object RevertDeductibleToConfirmed : SelectTierEvent

  data object Continue : SelectTierEvent

  data object ClearNavigation : SelectTierEvent
}
