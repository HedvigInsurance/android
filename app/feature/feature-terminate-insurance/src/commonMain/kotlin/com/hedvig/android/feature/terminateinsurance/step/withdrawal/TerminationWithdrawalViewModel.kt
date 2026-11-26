package com.hedvig.android.feature.terminateinsurance.step.withdrawal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
  contractWithdrawalRepository: ContractWithdrawalRepository,
  clock: Clock,
  backstack: Backstack,
) : MoleculeViewModel<TerminationWithdrawalEvent, TerminationWithdrawalUiState>(
    initialState = TerminationWithdrawalUiState.Loading,
    presenter = TerminationWithdrawalPresenter(
      insuranceInfo = insuranceInfo,
      contractWithdrawalRepository = contractWithdrawalRepository,
      clock = clock,
      backstack = backstack,
    ),
  )

internal class TerminationWithdrawalPresenter(
  private val insuranceInfo: TerminationGraphParameters,
  private val contractWithdrawalRepository: ContractWithdrawalRepository,
  private val clock: Clock,
  private val backstack: Backstack,
) : MoleculePresenter<TerminationWithdrawalEvent, TerminationWithdrawalUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationWithdrawalEvent>.present(
    lastState: TerminationWithdrawalUiState,
  ): TerminationWithdrawalUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var uiState by remember {
      mutableStateOf(
        if (lastState is TerminationWithdrawalUiState.Content) lastState.copy(isSubmitting = false) else lastState,
      )
    }

    LaunchedEffect(loadIteration) {
      if (uiState is TerminationWithdrawalUiState.Content) return@LaunchedEffect
      uiState = TerminationWithdrawalUiState.Loading
      uiState = contractWithdrawalRepository.getContractWithdrawal(insuranceInfo.contractId).fold(
        ifLeft = { TerminationWithdrawalUiState.Failure },
        ifRight = { withdrawal ->
          TerminationWithdrawalUiState.Content(
            insuranceInfo = insuranceInfo,
            statusText = withdrawal.statusText,
            action = withdrawal.action,
            endDate = withdrawalEndDate(withdrawal.action, clock),
            confirmationEmail = withdrawal.confirmationEmail,
            submitError = null,
            isSubmitting = false,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        TerminationWithdrawalEvent.Retry -> {
          loadIteration++
        }

        TerminationWithdrawalEvent.DismissSubmitError -> {
          val content = uiState as? TerminationWithdrawalUiState.Content ?: return@CollectEvents
          uiState = content.copy(submitError = null)
        }

        TerminationWithdrawalEvent.Submit -> {
          val content = uiState as? TerminationWithdrawalUiState.Content ?: return@CollectEvents
          if (content.isSubmitting) return@CollectEvents
          uiState = content.copy(isSubmitting = true, submitError = null)
          launch {
            contractWithdrawalRepository.withdrawContract(insuranceInfo.contractId).fold(
              ifLeft = {
                uiState = content.copy(isSubmitting = false, submitError = TerminationSubmitError.Generic)
              },
              ifRight = { result ->
                when (result) {
                  is WithdrawalResult.UserError -> {
                    uiState = content.copy(
                      isSubmitting = false,
                      submitError = TerminationSubmitError.UserError(result.message),
                    )
                  }

                  WithdrawalResult.Withdrawn -> {
                    backstack.replaceTerminationFlow(WithdrawalSuccessKey(content.endDate))
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
  data object Retry : TerminationWithdrawalEvent

  data object Submit : TerminationWithdrawalEvent

  data object DismissSubmitError : TerminationWithdrawalEvent
}

internal sealed interface TerminationWithdrawalUiState {
  data object Loading : TerminationWithdrawalUiState

  data object Failure : TerminationWithdrawalUiState

  data class Content(
    val insuranceInfo: TerminationGraphParameters,
    val statusText: String,
    val action: TerminationAction,
    val endDate: LocalDate,
    val confirmationEmail: String,
    val submitError: TerminationSubmitError?,
    val isSubmitting: Boolean,
  ) : TerminationWithdrawalUiState
}

/**
 * An active contract comes as [TerminationAction.TerminateWithDate] with `minDate == maxDate`, the only date it can
 * end on. A contract that has not started yet is deleted, which takes effect today.
 */
internal fun withdrawalEndDate(action: TerminationAction, clock: Clock): LocalDate = when (action) {
  is TerminationAction.TerminateWithDate -> action.minDate
  is TerminationAction.DeleteInsurance -> clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
