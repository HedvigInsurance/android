package com.hedvig.android.feature.terminateinsurance.step.withdrawal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.terminateinsurance.data.ContractWithdrawalRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.WithdrawalResult
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.WithdrawalSuccessKey
import com.hedvig.android.feature.terminateinsurance.navigation.replaceTerminationFlow
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSubmitError
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HedvigViewModel(ActivityRetainedScope::class)
internal class TerminationWithdrawalViewModel @AssistedInject constructor(
  @Assisted insuranceInfo: TerminationGraphParameters,
  @Assisted statusText: String,
  @Assisted action: TerminationAction,
  contractWithdrawalRepository: ContractWithdrawalRepository,
  clock: Clock,
  backstack: Backstack,
) : MoleculeViewModel<TerminationWithdrawalEvent, TerminationWithdrawalUiState>(
    initialState = TerminationWithdrawalUiState(
      insuranceInfo = insuranceInfo,
      statusText = statusText,
      action = action,
      endDate = withdrawalEndDate(action, clock),
      submitError = null,
      isSubmitting = false,
    ),
    presenter = TerminationWithdrawalPresenter(
      contractId = insuranceInfo.contractId,
      contractWithdrawalRepository = contractWithdrawalRepository,
      backstack = backstack,
    ),
  )

internal class TerminationWithdrawalPresenter(
  private val contractId: String,
  private val contractWithdrawalRepository: ContractWithdrawalRepository,
  private val backstack: Backstack,
) : MoleculePresenter<TerminationWithdrawalEvent, TerminationWithdrawalUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationWithdrawalEvent>.present(
    lastState: TerminationWithdrawalUiState,
  ): TerminationWithdrawalUiState {
    var uiState by remember { mutableStateOf(lastState.copy(isSubmitting = false)) }

    CollectEvents { event ->
      when (event) {
        TerminationWithdrawalEvent.DismissSubmitError -> {
          uiState = uiState.copy(submitError = null)
        }

        TerminationWithdrawalEvent.Submit -> {
          if (uiState.isSubmitting) return@CollectEvents
          uiState = uiState.copy(isSubmitting = true, submitError = null)
          launch {
            contractWithdrawalRepository.withdrawContract(contractId).fold(
              ifLeft = {
                uiState = uiState.copy(isSubmitting = false, submitError = TerminationSubmitError.Generic)
              },
              ifRight = { result ->
                when (result) {
                  is WithdrawalResult.UserError -> {
                    uiState = uiState.copy(
                      isSubmitting = false,
                      submitError = TerminationSubmitError.UserError(result.message),
                    )
                  }

                  WithdrawalResult.Withdrawn -> {
                    backstack.replaceTerminationFlow(WithdrawalSuccessKey(uiState.endDate))
                  }
                }
              },
            )
          }
        }
      }
    }

    return uiState
  }
}

internal sealed interface TerminationWithdrawalEvent {
  data object Submit : TerminationWithdrawalEvent

  data object DismissSubmitError : TerminationWithdrawalEvent
}

internal data class TerminationWithdrawalUiState(
  val insuranceInfo: TerminationGraphParameters,
  val statusText: String,
  val action: TerminationAction,
  val endDate: LocalDate,
  val submitError: TerminationSubmitError?,
  val isSubmitting: Boolean,
)

/**
 * An active contract comes as [TerminationAction.TerminateWithDate] with `minDate == maxDate`, the only date it can
 * end on. A contract that has not started yet is deleted, which takes effect today.
 */
internal fun withdrawalEndDate(action: TerminationAction, clock: Clock): LocalDate = when (action) {
  is TerminationAction.TerminateWithDate -> action.minDate
  is TerminationAction.DeleteInsurance -> clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
