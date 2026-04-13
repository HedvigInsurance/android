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
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TerminationConfirmationViewModel(
  terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  insuranceInfo: TerminationGraphParameters,
  extraCoverageItems: List<ExtraCoverageItem>,
  selectedReasonId: String,
  feedbackComment: String?,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  clock: Clock,
) : MoleculeViewModel<TerminationConfirmationEvent, OverviewUiState>(
    OverviewUiState(
      terminationType = terminationType,
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      notificationMessage = null,
      terminationSuccess = null,
      userError = null,
      isSubmittingContractTermination = false,
    ),
    TerminationConfirmationPresenter(
      terminationType,
      insuranceInfo,
      selectedReasonId,
      feedbackComment,
      terminateInsuranceRepository,
      getTerminationNotificationUseCase,
      clock,
    ),
  )

sealed interface TerminationConfirmationEvent {
  data object Submit : TerminationConfirmationEvent

  data object HandledNavigation : TerminationConfirmationEvent
}

internal class TerminationConfirmationPresenter(
  private val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  private val insuranceInfo: TerminationGraphParameters,
  private val selectedReasonId: String,
  private val feedbackComment: String?,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  private val clock: Clock,
) : MoleculePresenter<TerminationConfirmationEvent, OverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationConfirmationEvent>.present(
    lastState: OverviewUiState,
  ): OverviewUiState {
    var uiState by remember { mutableStateOf(lastState) }

    val notificationMessage by produceState<String?>(lastState.notificationMessage) {
      val terminationDate = when (terminationType) {
        Deletion -> clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        is Termination -> terminationType.terminationDate
      }
      getTerminationNotificationUseCase.invoke(
        contractId = insuranceInfo.contractId,
        terminationDate = terminationDate,
      ).collect {
        value = it.getOrNull()
      }
    }

    CollectEvents { event ->
      when (event) {
        TerminationConfirmationEvent.HandledNavigation -> {
          uiState = uiState.copy(terminationSuccess = null, userError = null)
        }

        TerminationConfirmationEvent.Submit -> {
          uiState = uiState.copy(isSubmittingContractTermination = true, userError = null)
          launch {
            val result = when (terminationType) {
              Deletion -> terminateInsuranceRepository.deleteContract(
                contractId = insuranceInfo.contractId,
                surveyOptionId = selectedReasonId,
                comment = feedbackComment,
              )

              is Termination -> terminateInsuranceRepository.terminateContract(
                contractId = insuranceInfo.contractId,
                terminationDate = terminationType.terminationDate,
                surveyOptionId = selectedReasonId,
                comment = feedbackComment,
              )
            }
            result.fold(
              ifLeft = { errorMessage ->
                uiState = uiState.copy(
                  isSubmittingContractTermination = false,
                  userError = errorMessage.message,
                )
              },
              ifRight = { terminationResult ->
                when (terminationResult) {
                  is TerminationResult.UserError -> uiState = uiState.copy(
                    isSubmittingContractTermination = false,
                    userError = terminationResult.message,
                  )

                  is TerminationResult.Terminated -> uiState = uiState.copy(
                    isSubmittingContractTermination = false,
                    terminationSuccess = TerminationSuccessResult(terminationResult.terminationDate),
                  )

                  TerminationResult.Deleted -> uiState = uiState.copy(
                    isSubmittingContractTermination = false,
                    terminationSuccess = TerminationSuccessResult(terminationDate = null),
                  )
                }
              },
            )
          }
        }
      }
    }

    return uiState.copy(notificationMessage = notificationMessage)
  }
}

internal data class TerminationSuccessResult(val terminationDate: LocalDate?)

internal data class OverviewUiState(
  val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  val insuranceInfo: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val notificationMessage: String?,
  val terminationSuccess: TerminationSuccessResult?,
  val userError: String?,
  val isSubmittingContractTermination: Boolean,
)
