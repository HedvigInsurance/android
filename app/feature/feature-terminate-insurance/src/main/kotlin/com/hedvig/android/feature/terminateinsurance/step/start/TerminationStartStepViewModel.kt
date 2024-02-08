package com.hedvig.android.feature.terminateinsurance.step.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TerminationStartStepViewModel(
  private val insuranceId: InsuranceId,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<TerminationStartStepEvent, TerminationStartStepUiState>(
    TerminationStartStepUiState.Initial,
    TerminationStartStepPresenter(insuranceId, terminateInsuranceRepository),
  )

private class TerminationStartStepPresenter(
  private val insuranceId: InsuranceId,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<TerminationStartStepEvent, TerminationStartStepUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationStartStepEvent>.present(
    lastState: TerminationStartStepUiState,
  ): TerminationStartStepUiState {
    var loadingNextStepIteration by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var isLoadingNextStep by remember { mutableStateOf(false) }
    var terminationStep: TerminateInsuranceStep? by remember { mutableStateOf(null) }

    LaunchedEffect(loadingNextStepIteration) {
      isLoadingNextStep = false
      if (loadingNextStepIteration == 0) return@LaunchedEffect
      isLoadingNextStep = true
      hasError = false
      terminateInsuranceRepository.startTerminationFlow(insuranceId).fold(
        ifLeft = {
          hasError = true
        },
        ifRight = { step: TerminateInsuranceStep ->
          terminationStep = step
        },
      )
      isLoadingNextStep = false
    }

    CollectEvents { event ->
      when (event) {
        TerminationStartStepEvent.InitiateTerminationFlow -> loadingNextStepIteration++
        TerminationStartStepEvent.HandledNextStep -> terminationStep = null
        TerminationStartStepEvent.HandledShowingNetworkError -> hasError = false
      }
    }

    return TerminationStartStepUiState(
      failedToLoadNextStep = hasError,
      isLoadingNextStep = isLoadingNextStep,
      nextStep = terminationStep,
    )
  }
}

internal data class TerminationStartStepUiState(
  val failedToLoadNextStep: Boolean,
  val isLoadingNextStep: Boolean,
  val nextStep: TerminateInsuranceStep?,
) {
  companion object {
    val Initial = TerminationStartStepUiState(false, false, null)
  }
}

internal sealed interface TerminationStartStepEvent {
  data object InitiateTerminationFlow : TerminationStartStepEvent

  data object HandledShowingNetworkError : TerminationStartStepEvent

  data object HandledNextStep : TerminationStartStepEvent
}
