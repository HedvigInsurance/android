package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.addon.purchase.data.SubmitAddonPurchaseUseCase
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Content
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class AddonSummaryViewModel(
  summaryParameters: SummaryParameters,
  submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
) : MoleculeViewModel<AddonSummaryEvent, AddonSummaryState>(
    initialState = Loading,
    presenter = AddonSummaryPresenter(summaryParameters, submitAddonPurchaseUseCase),
  )

internal class AddonSummaryPresenter(
  private val summaryParameters: SummaryParameters,
  private val submitAddonPurchaseUseCase: SubmitAddonPurchaseUseCase,
) :
  MoleculePresenter<AddonSummaryEvent, AddonSummaryState> {
  @Composable
  override fun MoleculePresenterScope<AddonSummaryEvent>.present(lastState: AddonSummaryState): AddonSummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    val initialState = Content(summaryParameters)

    CollectEvents { event ->
      when (event) {
        AddonSummaryEvent.Submit -> submitIteration++
        AddonSummaryEvent.ReturnToInitialState -> currentState = initialState
      }
    }

    LaunchedEffect(Unit) {
      currentState = initialState
    }

    LaunchedEffect(submitIteration) {
      currentState = Loading
      if (submitIteration > 0) {
        submitAddonPurchaseUseCase.invoke(
          quoteId = summaryParameters.quote.quoteId,
          addonId = summaryParameters.quote.addonId,
        ).fold(
          ifLeft = {
            currentState = initialState.copy(navigateToFailure = true)
          },
          ifRight = { date ->
            currentState = initialState.copy(activationDateForSuccessfullyPurchasedAddon = date)
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface AddonSummaryState {
  data object Loading : AddonSummaryState

  data class Content(
    val summaryParameters: SummaryParameters,
    val activationDateForSuccessfullyPurchasedAddon: LocalDate? = null,
    val navigateToFailure: Boolean = false,
  ) : AddonSummaryState
}

internal sealed interface AddonSummaryEvent {
  data object Submit : AddonSummaryEvent

  data object ReturnToInitialState : AddonSummaryEvent
}
