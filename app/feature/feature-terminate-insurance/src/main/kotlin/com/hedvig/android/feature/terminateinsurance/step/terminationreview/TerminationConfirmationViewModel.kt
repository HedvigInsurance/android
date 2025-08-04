package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationNotification
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TerminationConfirmationViewModel(
  private val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  private val insuranceInfo: TerminationGraphParameters,
  private val extraCoverageItems: List<ExtraCoverageItem>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  private val clock: Clock,
) : MoleculeViewModel<TerminationConfirmationEvent, OverviewUiState>(
    OverviewUiState(
      terminationType = terminationType,
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      notification = null,
      nextStep = null,
      errorMessage = null,
      isSubmittingContractTermination = false,
    ),
    TerminationConfirmationPresenter(
      terminationType,
      insuranceInfo,
      terminateInsuranceRepository,
      getTerminationNotificationUseCase,
      clock,
    ),
  )

sealed interface TerminationConfirmationEvent {
  data object Submit : TerminationConfirmationEvent

  data object HandledNextStepNavigation : TerminationConfirmationEvent
}

private class TerminationConfirmationPresenter(
  private val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  private val insuranceInfo: TerminationGraphParameters,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  private val clock: Clock,
) : MoleculePresenter<TerminationConfirmationEvent, OverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationConfirmationEvent>.present(
    lastState: OverviewUiState,
  ): OverviewUiState {
    var uiState by remember { mutableStateOf(lastState) }
    val notification by produceState(lastState.notification) {
      getTerminationNotificationUseCase.invoke(
        contractId = insuranceInfo.contractId,
        terminationDate = when (terminationType) {
          Deletion -> clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
          is Termination -> terminationType.terminationDate
        },
      ).collect {
        value = it.getOrNull()
      }
    }

    CollectEvents { event ->
      when (event) {
        TerminationConfirmationEvent.HandledNextStepNavigation -> {
          uiState = uiState.copy(nextStep = null)
        }

        TerminationConfirmationEvent.Submit -> {
          uiState = uiState.copy(isSubmittingContractTermination = true)
          launch {
            when (terminationType) {
              Deletion -> {
                terminateInsuranceRepository.confirmDeletion()
              }

              is Termination -> {
                terminateInsuranceRepository.setTerminationDate(terminationType.terminationDate)
              }
            }.fold(
              ifLeft = { errorMessage ->
                uiState = uiState.copy(
                  isSubmittingContractTermination = false,
                  errorMessage = errorMessage.message,
                )
              },
              ifRight = { terminateInsuranceFlowStep ->
                uiState = uiState.copy(
                  isSubmittingContractTermination = false,
                  nextStep = terminateInsuranceFlowStep,
                )
              },
            )
          }
        }
      }
    }

    return uiState.copy(notification = notification)
  }
}

internal data class OverviewUiState(
  val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  val insuranceInfo: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val notification: TerminationNotification?,
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isSubmittingContractTermination: Boolean,
)
