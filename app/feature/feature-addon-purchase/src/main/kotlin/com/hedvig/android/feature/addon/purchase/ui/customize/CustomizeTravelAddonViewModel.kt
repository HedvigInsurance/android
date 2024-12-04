package com.hedvig.android.feature.addon.purchase.ui.customize

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class CustomizeTravelAddonViewModel() : MoleculeViewModel<CustomizeTravelAddonEvent, CustomizeTravelAddonState>(
  initialState = CustomizeTravelAddonState.Loading,
  presenter = CustomizeTravelAddonPresenter(),
)

internal class CustomizeTravelAddonPresenter() : MoleculePresenter<CustomizeTravelAddonEvent, CustomizeTravelAddonState> {
  @Composable
  override fun MoleculePresenterScope<CustomizeTravelAddonEvent>.present(
    lastState: CustomizeTravelAddonState,
  ): CustomizeTravelAddonState {
    CollectEvents { event ->
      when (event) {
        CustomizeTravelAddonEvent.Reload -> TODO()
        is CustomizeTravelAddonEvent.ChooseOptionInDialog -> TODO()
        CustomizeTravelAddonEvent.ChooseSelectedOption -> TODO()
        CustomizeTravelAddonEvent.SetOptionBackToPreviouslyChosen -> TODO()
      }
    }

    TODO("Not yet implemented")
  }
}

internal sealed interface CustomizeTravelAddonState {
  data object Loading : CustomizeTravelAddonState

  data class Success(
    val travelAddonOffer: TravelAddonOffer,
    val currentlyChosenOption: TravelAddonQuote,
    val currentlyChosenOptionInDialog: TravelAddonQuote,
  ) : CustomizeTravelAddonState

  data class Failure(val errorMessage: String? = null) : CustomizeTravelAddonState
}

internal sealed interface CustomizeTravelAddonEvent {
  data object Reload : CustomizeTravelAddonEvent

  data class ChooseOptionInDialog(val option: TravelAddonQuote) : CustomizeTravelAddonEvent

  data object ChooseSelectedOption : CustomizeTravelAddonEvent

  data object SetOptionBackToPreviouslyChosen : CustomizeTravelAddonEvent
}
