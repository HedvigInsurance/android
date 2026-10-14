package com.hedvig.android.feature.inapppurchase.ui.offers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.navigation.InAppPurchaseOffersKey
import com.hedvig.android.feature.inapppurchase.navigation.InAppPurchaseSummaryKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class InAppPurchaseOffersViewModel(
  @Assisted params: InAppPurchaseOffersKey,
  backstack: Backstack,
) : MoleculeViewModel<InAppPurchaseOffersEvent, InAppPurchaseOffersUiState>(
    initialState = InAppPurchaseOffersUiState(params.productDisplayName, params.offers, params.offers.first()),
    presenter = InAppPurchaseOffersPresenter(params, backstack),
  )

internal class InAppPurchaseOffersPresenter(
  private val params: InAppPurchaseOffersKey,
  private val backstack: Backstack,
) : MoleculePresenter<InAppPurchaseOffersEvent, InAppPurchaseOffersUiState> {
  @Composable
  override fun MoleculePresenterScope<InAppPurchaseOffersEvent>.present(
    lastState: InAppPurchaseOffersUiState,
  ): InAppPurchaseOffersUiState {
    var selectedOffer by remember { mutableStateOf(lastState.selectedOffer) }

    CollectEvents { event ->
      when (event) {
        is InAppPurchaseOffersEvent.SelectOffer -> {
          selectedOffer = params.offers.firstOrNull { it.id == event.offerId } ?: selectedOffer
        }

        InAppPurchaseOffersEvent.Continue -> {
          backstack.add(InAppPurchaseSummaryKey(params.productDisplayName, params.shopSessionId, selectedOffer))
        }
      }
    }

    return InAppPurchaseOffersUiState(params.productDisplayName, params.offers, selectedOffer)
  }
}

internal sealed interface InAppPurchaseOffersEvent {
  data class SelectOffer(val offerId: String) : InAppPurchaseOffersEvent

  data object Continue : InAppPurchaseOffersEvent
}

internal data class InAppPurchaseOffersUiState(
  val productDisplayName: String,
  val offers: List<InAppPurchaseOffer>,
  val selectedOffer: InAppPurchaseOffer,
)
