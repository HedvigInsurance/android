package com.hedvig.android.feature.terminateinsurance.step.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class TerminationOfferViewModel(
  title: String,
  description: String,
  buttonTitle: String,
  skipButtonTitle: String,
  action: OfferAction,
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<TerminationOfferEvent, TerminationOfferUiState>(
    initialState = TerminationOfferUiState.Content(
      title = title,
      description = description,
      buttonTitle = buttonTitle,
      skipButtonTitle = skipButtonTitle,
      action = action,
    ),
    presenter = TerminationOfferPresenter(terminateInsuranceRepository),
  )

private class TerminationOfferPresenter(
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<TerminationOfferEvent, TerminationOfferUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationOfferEvent>.present(
    lastState: TerminationOfferUiState,
  ): TerminationOfferUiState {
    var skipIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        TerminationOfferEvent.Skip -> {
          skipIteration++
        }

        TerminationOfferEvent.ClearNextStep -> {
          val state = currentState as? TerminationOfferUiState.Content ?: return@CollectEvents
          currentState = state.copy(nextStep = null, skipLoading = false)
        }
      }
    }

    LaunchedEffect(skipIteration) {
      if (skipIteration > 0) {
        val state = currentState as? TerminationOfferUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(skipLoading = true)
        currentState = terminateInsuranceRepository.skipOfferStep().fold(
          ifLeft = { TerminationOfferUiState.Error },
          ifRight = { step -> state.copy(skipLoading = false, nextStep = step) },
        )
      }
    }

    return currentState
  }
}

internal sealed interface TerminationOfferUiState {
  data class Content(
    val title: String,
    val description: String,
    val buttonTitle: String,
    val skipButtonTitle: String,
    val action: OfferAction,
    val skipLoading: Boolean = false,
    val nextStep: TerminateInsuranceStep? = null,
  ) : TerminationOfferUiState

  data object Error : TerminationOfferUiState
}

internal sealed interface TerminationOfferEvent {
  data object Skip : TerminationOfferEvent

  data object ClearNextStep : TerminationOfferEvent
}
