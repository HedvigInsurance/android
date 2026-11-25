package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationChooseActionKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey
import com.hedvig.android.feature.terminateinsurance.navigation.replaceTerminationFlow
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@HedvigViewModel(ActivityRetainedScope::class)
internal class ChooseInsuranceToTerminateViewModel @AssistedInject constructor(
  @Assisted insuranceId: String?,
  getTerminatableContractsUseCase: GetTerminatableContractsUseCase,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  backstack: Backstack,
) : MoleculeViewModel<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState>(
    initialState = ChooseInsuranceToTerminateStepUiState.Loading,
    presenter = ChooseInsuranceToTerminatePresenter(
      insuranceId = insuranceId,
      getTerminatableContractsUseCase = getTerminatableContractsUseCase,
      terminateInsuranceRepository = terminateInsuranceRepository,
      backstack = backstack,
    ),
  )

internal class ChooseInsuranceToTerminatePresenter(
  private val insuranceId: String?,
  private val getTerminatableContractsUseCase: GetTerminatableContractsUseCase,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val backstack: Backstack,
) : MoleculePresenter<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToTerminateEvent>.present(
    lastState: ChooseInsuranceToTerminateStepUiState,
  ): ChooseInsuranceToTerminateStepUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var nextStepRequest: NextStepRequest? by remember { mutableStateOf(null) }
    var hasTriedSkippingChooseStep by remember { mutableStateOf(false) }
    var isSkippingChooseStep by remember { mutableStateOf(false) }
    val initialSelected = if (lastState is ChooseInsuranceToTerminateStepUiState.Success) {
      lastState.selectedInsurance?.id
    } else {
      insuranceId
    }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceToTerminateEvent.RetryLoadData -> {
          loadIteration++
        }

        is ChooseInsuranceToTerminateEvent.SubmitSelectedInsuranceToTerminate -> {
          nextStepRequest = NextStepRequest(event.insurance, skipsChooseStep = false)
        }

        is ChooseInsuranceToTerminateEvent.SelectInsurance -> {
          val currentStateValue = currentState
          if (currentStateValue is ChooseInsuranceToTerminateStepUiState.Success) {
            val newlySelectedInsurance = currentStateValue.insuranceList.first { it.id == event.insuranceId }
            currentState = currentStateValue.copy(
              selectedInsurance = newlySelectedInsurance,
              navigationStepFailedToLoad = false,
            )
          }
        }
      }
    }

    LaunchedEffect(nextStepRequest) {
      val currentStateValue = currentState as? ChooseInsuranceToTerminateStepUiState.Success ?: return@LaunchedEffect
      val request = nextStepRequest ?: return@LaunchedEffect
      val terminatableInsurance = request.insurance
      // A skipped choose step must not stay underneath, so Back from the next step leaves the flow.
      val navigateToNextStep: (HedvigNavKey) -> Unit = { key ->
        if (request.skipsChooseStep) backstack.replaceTerminationFlow(key) else backstack.add(key)
      }
      val commonParams = TerminationGraphParameters(
        terminatableInsurance.id,
        terminatableInsurance.displayName,
        terminatableInsurance.contractExposure,
        terminatableInsurance.contractGroup,
      )
      if (terminatableInsurance.supportsWithdrawal) {
        navigateToNextStep(TerminationChooseActionKey(commonParams))
        nextStepRequest = null
        return@LaunchedEffect
      }
      val loadingState = currentStateValue.copy(
        isNavigationStepLoading = true,
        navigationStepFailedToLoad = false,
      )
      currentState = loadingState
      currentState = terminateInsuranceRepository
        .getTerminationSurvey(terminatableInsurance.id)
        .fold(
          ifLeft = {
            isSkippingChooseStep = false
            loadingState.copy(
              navigationStepFailedToLoad = true,
              isNavigationStepLoading = false,
            )
          },
          ifRight = { surveyData ->
            navigateToNextStep(
              TerminationSurveyFirstStepKey(
                options = surveyData.options,
                action = surveyData.action,
                commonParams = commonParams,
              ),
            )
            loadingState.copy(isNavigationStepLoading = false)
          },
        )
      nextStepRequest = null
    }

    LaunchedEffect(loadIteration) {
      if (lastState !is ChooseInsuranceToTerminateStepUiState.Success) {
        currentState = ChooseInsuranceToTerminateStepUiState.Loading
      }
      getTerminatableContractsUseCase.invoke().collect { contractsResult ->
        contractsResult.fold(
          ifLeft = {
            logcat(priority = LogPriority.INFO) { "Cannot load contracts for cancellation" }
            currentState = ChooseInsuranceToTerminateStepUiState.Failure
          },
          ifRight = { eligibleInsurances ->
            logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for cancellation" }
            currentState = if (eligibleInsurances == null) {
              ChooseInsuranceToTerminateStepUiState.NotAllowed
            } else {
              val selectedInsurance = if (eligibleInsurances.size == 1) {
                eligibleInsurances[0]
              } else {
                eligibleInsurances.firstOrNull { it.id == initialSelected }
              }
              if (selectedInsurance != null && !hasTriedSkippingChooseStep) {
                hasTriedSkippingChooseStep = true
                isSkippingChooseStep = true
                nextStepRequest = NextStepRequest(selectedInsurance, skipsChooseStep = true)
              }
              ChooseInsuranceToTerminateStepUiState.Success(
                insuranceList = eligibleInsurances,
                selectedInsurance = selectedInsurance,
                isNavigationStepLoading = false,
                navigationStepFailedToLoad = false,
              )
            }
          },
        )
      }
    }
    return if (isSkippingChooseStep) ChooseInsuranceToTerminateStepUiState.Loading else currentState
  }
}

private data class NextStepRequest(
  val insurance: TerminatableInsurance,
  val skipsChooseStep: Boolean,
)

internal sealed interface ChooseInsuranceToTerminateEvent {
  data class SelectInsurance(val insuranceId: String) :
    ChooseInsuranceToTerminateEvent

  data object RetryLoadData : ChooseInsuranceToTerminateEvent

  data class SubmitSelectedInsuranceToTerminate(val insurance: TerminatableInsurance) :
    ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<TerminatableInsurance>,
    val selectedInsurance: TerminatableInsurance?,
    val isNavigationStepLoading: Boolean,
    val navigationStepFailedToLoad: Boolean,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}
