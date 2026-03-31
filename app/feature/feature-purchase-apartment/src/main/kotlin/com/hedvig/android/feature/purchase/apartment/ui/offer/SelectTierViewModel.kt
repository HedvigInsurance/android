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
    SelectTierUiState(
      offers = params.offers,
      selectedOfferId = params.offers.firstOrNull { it.tierDisplayName == "Standard" }?.offerId
        ?: params.offers.firstOrNull()?.offerId
        ?: "",
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = null,
    ),
    SelectTierPresenter(params),
  )

internal class SelectTierPresenter(
  private val params: SelectTierParameters,
) : MoleculePresenter<SelectTierEvent, SelectTierUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectTierEvent>.present(lastState: SelectTierUiState): SelectTierUiState {
    var selectedOfferId by remember { mutableStateOf(lastState.selectedOfferId) }
    var summaryToNavigate: SummaryParameters? by remember { mutableStateOf(lastState.summaryToNavigate) }

    CollectEvents { event ->
      when (event) {
        is SelectTierEvent.SelectOffer -> {
          selectedOfferId = event.offerId
        }

        SelectTierEvent.Continue -> {
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
      offers = params.offers,
      selectedOfferId = selectedOfferId,
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = summaryToNavigate,
    )
  }
}

internal data class SelectTierUiState(
  val offers: List<TierOfferData>,
  val selectedOfferId: String,
  val shopSessionId: String,
  val productDisplayName: String,
  val summaryToNavigate: SummaryParameters?,
)

internal sealed interface SelectTierEvent {
  data class SelectOffer(val offerId: String) : SelectTierEvent

  data object Continue : SelectTierEvent

  data object ClearNavigation : SelectTierEvent
}
