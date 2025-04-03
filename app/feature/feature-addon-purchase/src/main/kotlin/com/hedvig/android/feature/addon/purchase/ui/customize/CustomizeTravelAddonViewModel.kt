package com.hedvig.android.feature.addon.purchase.ui.customize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCase
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class CustomizeTravelAddonViewModel(
  insuranceId: String,
  getTravelAddonOfferUseCase: GetTravelAddonOfferUseCase,
) : MoleculeViewModel<CustomizeTravelAddonEvent, CustomizeTravelAddonState>(
    initialState = CustomizeTravelAddonState.Loading,
    presenter = CustomizeTravelAddonPresenter(
      getTravelAddonOfferUseCase = getTravelAddonOfferUseCase,
      insuranceId = insuranceId,
    ),
  )

internal class CustomizeTravelAddonPresenter(
  private val insuranceId: String,
  private val getTravelAddonOfferUseCase: GetTravelAddonOfferUseCase,
) : MoleculePresenter<CustomizeTravelAddonEvent, CustomizeTravelAddonState> {
  @Composable
  override fun MoleculePresenterScope<CustomizeTravelAddonEvent>.present(
    lastState: CustomizeTravelAddonState,
  ): CustomizeTravelAddonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var selectedOptionInDialog by remember {
      mutableStateOf<TravelAddonQuote?>(
        if (lastState is CustomizeTravelAddonState.Success) {
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
          val state = currentState as? CustomizeTravelAddonState.Success ?: return@CollectEvents
          val optionInDialog = selectedOptionInDialog ?: return@CollectEvents
          currentState = state.copy(
            currentlyChosenOption = optionInDialog,
          )
        }

        CustomizeTravelAddonEvent.SetOptionBackToPreviouslyChosen -> {
          val state = currentState as? CustomizeTravelAddonState.Success ?: return@CollectEvents
          selectedOptionInDialog = state.currentlyChosenOption
        }

        CustomizeTravelAddonEvent.ClearNavigation -> {
          val state = currentState as? CustomizeTravelAddonState.Success ?: return@CollectEvents
          currentState = state.copy(summaryParamsToNavigateFurther = null)
        }

        CustomizeTravelAddonEvent.SubmitSelected -> {
          val state = currentState as? CustomizeTravelAddonState.Success ?: return@CollectEvents
          currentState = state.copy(
            summaryParamsToNavigateFurther = SummaryParameters(
              offerDisplayName = state.travelAddonOffer.title,
              quote = state.currentlyChosenOption,
              activationDate = state.travelAddonOffer.activationDate,
              currentTravelAddon = state.travelAddonOffer.currentTravelAddon,
            ),
          )
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is CustomizeTravelAddonState.Success) return@LaunchedEffect
      currentState = CustomizeTravelAddonState.Loading
      getTravelAddonOfferUseCase.invoke(insuranceId).fold(
        ifLeft = { error ->
          currentState = CustomizeTravelAddonState.Failure(error.message)
        },
        ifRight = { offer ->
          selectedOptionInDialog = offer.addonOptions[0]
          currentState = CustomizeTravelAddonState.Success(
            travelAddonOffer = offer,
            currentlyChosenOption = offer.addonOptions[0],
            currentlyChosenOptionInDialog = selectedOptionInDialog,
            summaryParamsToNavigateFurther = null,
          )
        },
      )
    }

    val state = currentState
    return when (state) {
      is CustomizeTravelAddonState.Failure, is CustomizeTravelAddonState.Loading -> state
      is CustomizeTravelAddonState.Success -> state.copy(
        currentlyChosenOptionInDialog = selectedOptionInDialog,
      )
    }
  }
}

internal sealed interface CustomizeTravelAddonState {
  data object Loading : CustomizeTravelAddonState

  data class Success(
    val travelAddonOffer: TravelAddonOffer,
    val currentlyChosenOption: TravelAddonQuote,
    val currentlyChosenOptionInDialog: TravelAddonQuote?,
    val summaryParamsToNavigateFurther: SummaryParameters?,
  ) : CustomizeTravelAddonState

  data class Failure(val errorMessage: String? = null) : CustomizeTravelAddonState
}

internal sealed interface CustomizeTravelAddonEvent {
  data object Reload : CustomizeTravelAddonEvent

  data class ChooseOptionInDialog(val option: TravelAddonQuote) : CustomizeTravelAddonEvent

  data object ChooseSelectedOption : CustomizeTravelAddonEvent

  data object SetOptionBackToPreviouslyChosen : CustomizeTravelAddonEvent

  data object ClearNavigation : CustomizeTravelAddonEvent

  data object SubmitSelected : CustomizeTravelAddonEvent
}
