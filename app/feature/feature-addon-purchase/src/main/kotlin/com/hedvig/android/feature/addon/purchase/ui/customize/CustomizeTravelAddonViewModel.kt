package com.hedvig.android.feature.addon.purchase.ui.customize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.addon.purchase.data.AddonOffer
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class CustomizeTravelAddonViewModel(
  insuranceId: String,
  getTravelAddonOfferUseCase: GetTravelAddonOfferUseCase,
) : MoleculeViewModel<CustomizeTravelAddonEvent, CustomizeAddonState>(
    initialState = CustomizeAddonState.Loading,
    presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = getTravelAddonOfferUseCase,
      insuranceId = insuranceId,
    ),
  )

internal class CustomizeTravelAddonPresenter(
  private val insuranceId: String,
  private val getTravelAddonOfferUseCase: GetTravelAddonOfferUseCase,
) : MoleculePresenter<CustomizeTravelAddonEvent, CustomizeAddonState> {
  @Composable
  override fun MoleculePresenterScope<CustomizeTravelAddonEvent>.present(
    lastState: CustomizeAddonState,
  ): CustomizeAddonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var selectedOptionInDialog by remember {
      mutableStateOf<AddonQuote?>(
        if (lastState is CustomizeAddonState.Success.Selectable) {
          lastState.currentlyChosenOptionInDialog
        } else {
          null
        },
      )
    }
    CollectEvents { event ->
      when (event) {
        CustomizeTravelAddonEvent.Reload -> loadIteration++
        is CustomizeTravelAddonEvent.ChooseOptionInDialog -> {
          selectedOptionInDialog = event.option
        }

        CustomizeTravelAddonEvent.ChooseSelectedOption -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          val optionInDialog = selectedOptionInDialog ?: return@CollectEvents
          currentState = state.copy(
            currentlyChosenOption = optionInDialog,
          )
        }

        CustomizeTravelAddonEvent.SetOptionBackToPreviouslyChosen -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          selectedOptionInDialog = state.currentlyChosenOption
        }

        CustomizeTravelAddonEvent.ClearNavigation -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          currentState = state.copy(summaryParamsToNavigateFurther = null)
        }

        CustomizeTravelAddonEvent.SubmitSelected -> {
          val state = currentState as? CustomizeAddonState.Success.Selectable ?: return@CollectEvents
          currentState = state.copy(
            summaryParamsToNavigateFurther = SummaryParameters(
              offerDisplayName = state.travelAddonSelectableOffer.title,
              quote = state.currentlyChosenOption,
              activationDate = state.travelAddonSelectableOffer.activationDate,
              currentlyActiveAddon = state.travelAddonSelectableOffer.currentTravelAddon,
            ),
          )
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is CustomizeAddonState.Success.Selectable) return@LaunchedEffect //todo
      currentState = CustomizeAddonState.Loading
      getTravelAddonOfferUseCase.invoke(insuranceId).fold(
        ifLeft = { error ->
          currentState = CustomizeAddonState.Failure(error.message)
        },
        ifRight = { offer ->
          selectedOptionInDialog = offer.addonOptions[0]
          val extra = updateExtra(offer.currentTravelAddon, selectedOptionInDialog)
          currentState = CustomizeAddonState.Success.Selectable(
            travelAddonSelectableOffer = offer,
            currentlyChosenOption = offer.addonOptions[0],
            currentlyChosenOptionInDialog = selectedOptionInDialog,
            summaryParamsToNavigateFurther = null,
            // .currentlyChosenOption.itemCost.monthlyNet
            chosenOptionPremiumExtra = extra,
            currentlyActiveAddon = offer.currentTravelAddon,
          )
        },
      )
    }

    return when (val state = currentState) {
      is CustomizeAddonState.Failure, is CustomizeAddonState.Loading -> state
      is CustomizeAddonState.Success.Selectable -> state.copy(
        currentlyChosenOptionInDialog = selectedOptionInDialog,
        chosenOptionPremiumExtra = updateExtra(state.currentlyActiveAddon, selectedOptionInDialog),
      )

      is CustomizeAddonState.Success.Toggleable -> TODO()
    }
  }
}

private fun updateExtra(currentlyActiveAddon: CurrentlyActiveAddon?, chosenAddonQuote: AddonQuote?): UiMoney {
  return if (chosenAddonQuote == null) {
    // shouldn't happen
    UiMoney(0.0, UiCurrencyCode.SEK)
  } else if (currentlyActiveAddon == null) {
    chosenAddonQuote.itemCost.monthlyNet
  } else {
    val sum = chosenAddonQuote.itemCost.monthlyNet.amount - currentlyActiveAddon.netPremium.amount
    UiMoney(sum, chosenAddonQuote.itemCost.monthlyNet.currencyCode)
  }
}

internal sealed interface CustomizeAddonState {
  data object Loading : CustomizeAddonState

  sealed interface Success : CustomizeAddonState {
    data class Selectable(
      val travelAddonSelectableOffer: AddonOffer.Selectable,
      val currentlyChosenOption: AddonQuote,
      val currentlyChosenOptionInDialog: AddonQuote?,
      val summaryParamsToNavigateFurther: SummaryParameters?,
      val chosenOptionPremiumExtra: UiMoney,
      val currentlyActiveAddon: CurrentlyActiveAddon?,
    ) : Success

    data class Toggleable(
      val travelAddonOffer: AddonOffer.Selectable,
      val currentlyChosenOptions: List<AddonQuote>,
      val summaryParamsToNavigateFurther: SummaryParameters?,
      val totalPremiumExtra: UiMoney,
      val currentlyActiveAddons: List<CurrentlyActiveAddon>,
    ) : Success
  }

  data class Failure(val errorMessage: String? = null) : CustomizeAddonState
}

internal sealed interface CustomizeTravelAddonEvent {
  data object Reload : CustomizeTravelAddonEvent

  data class ChooseOptionInDialog(val option: AddonQuote) : CustomizeTravelAddonEvent

  data object ChooseSelectedOption : CustomizeTravelAddonEvent

  data object SetOptionBackToPreviouslyChosen : CustomizeTravelAddonEvent

  data object ClearNavigation : CustomizeTravelAddonEvent

  data object SubmitSelected : CustomizeTravelAddonEvent
}
