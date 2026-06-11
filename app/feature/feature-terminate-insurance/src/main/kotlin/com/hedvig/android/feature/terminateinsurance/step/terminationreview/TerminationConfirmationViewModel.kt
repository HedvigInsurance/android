package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationResult
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey.TerminationType.Deletion
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationConfirmationKey.TerminationType.Termination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSuccessKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TerminationConfirmationViewModel @AssistedInject constructor(
  @Assisted terminationType: TerminationConfirmationKey.TerminationType,
  @Assisted insuranceInfo: TerminationGraphParameters,
  @Assisted extraCoverageItems: List<ExtraCoverageItem>,
  @Assisted selectedReasonId: String,
  @Assisted feedbackComment: String?,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  clock: Clock,
  backstack: Backstack,
) : MoleculeViewModel<TerminationConfirmationEvent, OverviewUiState>(
    OverviewUiState(
      terminationType = terminationType,
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      notificationMessage = null,
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
      backstack,
    ),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(ActivityRetainedScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted terminationType: TerminationConfirmationKey.TerminationType,
      @Assisted insuranceInfo: TerminationGraphParameters,
      @Assisted extraCoverageItems: List<ExtraCoverageItem>,
      @Assisted selectedReasonId: String,
      @Assisted feedbackComment: String?,
    ): TerminationConfirmationViewModel
  }
}

sealed interface TerminationConfirmationEvent {
  data object Submit : TerminationConfirmationEvent
}

internal class TerminationConfirmationPresenter(
  private val terminationType: TerminationConfirmationKey.TerminationType,
  private val insuranceInfo: TerminationGraphParameters,
  private val selectedReasonId: String,
  private val feedbackComment: String?,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val getTerminationNotificationUseCase: GetTerminationNotificationUseCase,
  private val clock: Clock,
  private val backstack: Backstack,
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

                  is TerminationResult.Terminated -> backstack.navigateAndPopUpTo<TerminateInsuranceKey>(
                    TerminationSuccessKey(terminationResult.terminationDate),
                    inclusive = true,
                  )

                  TerminationResult.Deleted -> backstack.navigateAndPopUpTo<TerminateInsuranceKey>(
                    TerminationSuccessKey(terminationDate = null),
                    inclusive = true,
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

internal data class OverviewUiState(
  val terminationType: TerminationConfirmationKey.TerminationType,
  val insuranceInfo: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val notificationMessage: String?,
  val userError: String?,
  val isSubmittingContractTermination: Boolean,
)
