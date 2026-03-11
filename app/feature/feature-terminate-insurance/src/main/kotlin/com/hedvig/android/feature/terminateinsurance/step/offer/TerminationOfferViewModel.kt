package com.hedvig.android.feature.terminateinsurance.step.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.terminateinsurance.data.OfferAction
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
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
  changeTierRepository: ChangeTierRepository,
) : MoleculeViewModel<TerminationOfferEvent, TerminationOfferUiState>(
    initialState = TerminationOfferUiState.Content(
      title = title,
      description = description,
      buttonTitle = buttonTitle,
      skipButtonTitle = skipButtonTitle,
      action = action,
    ),
    presenter = TerminationOfferPresenter(terminateInsuranceRepository, changeTierRepository),
  )

private class TerminationOfferPresenter(
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val changeTierRepository: ChangeTierRepository,
) : MoleculePresenter<TerminationOfferEvent, TerminationOfferUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationOfferEvent>.present(
    lastState: TerminationOfferUiState,
  ): TerminationOfferUiState {
    var skipIteration by remember { mutableIntStateOf(0) }
    var loadChangeTier by remember { mutableStateOf(false) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        TerminationOfferEvent.Skip -> {
          skipIteration++
        }

        TerminationOfferEvent.ClearNextStep -> {
          val state = currentState as? TerminationOfferUiState.Content ?: return@CollectEvents
          currentState = state.copy(nextStep = null, skipLoading = false, changeTierIntent = null)
        }

        TerminationOfferEvent.FetchChangeTierIntent -> {
          loadChangeTier = true
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

    if (loadChangeTier) {
      LaunchedEffect(Unit) {
        val state = currentState as? TerminationOfferUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(ctaLoading = true)
        val insuranceId = terminateInsuranceRepository.getContractId()
        changeTierRepository.startChangeTierIntentAndGetQuotesId(
          insuranceId = insuranceId,
          source = TERMINATION_BETTER_COVERAGE,
        ).fold(
          ifLeft = { errorMessage ->
            logcat(LogPriority.ERROR) {
              "Received error while creating changeTierIntent from termination offer: $errorMessage"
            }
            currentState = state.copy(ctaLoading = false, changeTierError = true)
            loadChangeTier = false
          },
          ifRight = { changeTierResult ->
            val intent = changeTierResult.intentOutput
            if (intent != null && intent.quotes.isNotEmpty()) {
              currentState = state.copy(ctaLoading = false, changeTierIntent = insuranceId to intent)
              loadChangeTier = false
            } else {
              logcat(LogPriority.WARN) { "Change tier intent returned no quotes from termination offer" }
              currentState = state.copy(ctaLoading = false, changeTierError = true)
              loadChangeTier = false
            }
          },
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
    val ctaLoading: Boolean = false,
    val nextStep: TerminateInsuranceStep? = null,
    val changeTierIntent: Pair<String, IntentOutput>? = null,
    val changeTierError: Boolean = false,
  ) : TerminationOfferUiState

  data object Error : TerminationOfferUiState
}

internal sealed interface TerminationOfferEvent {
  data object Skip : TerminationOfferEvent

  data object FetchChangeTierIntent : TerminationOfferEvent

  data object ClearNextStep : TerminationOfferEvent
}
