package com.hedvig.android.feature.change.tier.ui.chooseinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.SELF_SERVICE
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.change.tier.data.CustomisableInsurance
import com.hedvig.android.feature.change.tier.data.GetCustomizableInsurancesUseCase
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceUiState.Loading
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseInsuranceViewModel(
  getCustomizableInsurancesUseCase: GetCustomizableInsurancesUseCase,
  tierRepository: ChangeTierRepository,
) : MoleculeViewModel<ChooseInsuranceToCustomizeEvent, ChooseInsuranceUiState>(
    initialState = Loading,
    presenter = ChooseInsurancePresenter(
      getCustomizableInsurancesUseCase = getCustomizableInsurancesUseCase,
      tierRepository = tierRepository,
    ),
  )

internal class ChooseInsurancePresenter(
  private val getCustomizableInsurancesUseCase: GetCustomizableInsurancesUseCase,
  private val tierRepository: ChangeTierRepository,
) : MoleculePresenter<ChooseInsuranceToCustomizeEvent, ChooseInsuranceUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToCustomizeEvent>.present(
    lastState: ChooseInsuranceUiState,
  ): ChooseInsuranceUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var insuranceToFetchIntentFor: CustomisableInsurance? by remember { mutableStateOf(null) }
    val initialSelected = if (lastState is ChooseInsuranceUiState.Success) {
      lastState.selectedInsurance?.id
    } else {
      null
    }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceToCustomizeEvent.RetryLoadData -> {
          loadIteration++
        }

        is ChooseInsuranceToCustomizeEvent.SubmitSelectedInsuranceToTerminate -> {
          insuranceToFetchIntentFor = event.insurance
        }

        is ChooseInsuranceToCustomizeEvent.SelectInsurance -> {
          val currentStateValue = currentState
          if (currentStateValue is ChooseInsuranceUiState.Success) {
            val newlySelectedInsurance = currentStateValue.insuranceList.first { it.id == event.insuranceId }
            currentState = currentStateValue.copy(
              selectedInsurance = newlySelectedInsurance,
              changeTierIntentFailedToLoad = false,
            )
          }
        }

        ChooseInsuranceToCustomizeEvent.ClearTerminationStep -> {
          val currentStateValue = currentState
          if (currentStateValue is ChooseInsuranceUiState.Success) {
            currentState = currentStateValue.copy(
              paramsToNavigateToNextStep = null,
              isChangeTierIntentLoading = false,
            )
          }
        }
      }
    }

    LaunchedEffect(insuranceToFetchIntentFor) {
      val currentStateValue = currentState as? ChooseInsuranceUiState.Success ?: return@LaunchedEffect
      currentState = currentStateValue.copy(
        changeTierIntentFailedToLoad = false,
      )
      val customisableInsurance = insuranceToFetchIntentFor ?: return@LaunchedEffect
      currentState = currentStateValue.copy(isChangeTierIntentLoading = true)
      currentState = tierRepository
        .startChangeTierIntentAndGetQuotesId(customisableInsurance.id, SELF_SERVICE)
        .fold(
          ifLeft = {
            currentStateValue.copy(
              changeTierIntentFailedToLoad = true,
              isChangeTierIntentLoading = false,
            )
          },
          ifRight = { intent ->
            val params = InsuranceCustomizationParameters(
              insuranceId = customisableInsurance.id,
              activationDateEpochDays = intent.activationDate.toEpochDays(),
              currentTierLevel = intent.currentTierLevel,
              currentTierName = intent.currentTierName,
              quoteIds = intent.quotes.map { it.id },
            )
            currentStateValue.copy(
              paramsToNavigateToNextStep = params,
              changeTierIntentFailedToLoad = false,
              isChangeTierIntentLoading = true,
            )
          },
        )
      insuranceToFetchIntentFor = null
    }

    LaunchedEffect(loadIteration) {
      if (lastState !is ChooseInsuranceUiState.Success) {
        currentState = ChooseInsuranceUiState.Loading
      }
      getCustomizableInsurancesUseCase.invoke().collect { contractsResult ->
        contractsResult.fold(
          ifLeft = {
            logcat(priority = LogPriority.INFO) { "Cannot load contracts for cancellation" }
            currentState = ChooseInsuranceUiState.Failure
          },
          ifRight = { eligibleInsurances ->
            logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for cancellation" }
            currentState = if (eligibleInsurances == null) {
              ChooseInsuranceUiState.NotAllowed
            } else {
              val selectedInsurance = if (eligibleInsurances.size == 1) {
                eligibleInsurances[0]
              } else {
                eligibleInsurances.firstOrNull { it.id == initialSelected }
              }
              ChooseInsuranceUiState.Success(
                eligibleInsurances,
                selectedInsurance,
                null,
                false,
                false,
              )
            }
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface ChooseInsuranceUiState {
  data object Loading : ChooseInsuranceUiState

  data class Success(
    val insuranceList: List<CustomisableInsurance>,
    val selectedInsurance: CustomisableInsurance?,
    val paramsToNavigateToNextStep: InsuranceCustomizationParameters? = null,
    val isChangeTierIntentLoading: Boolean = false,
    val changeTierIntentFailedToLoad: Boolean = false,
  ) : ChooseInsuranceUiState

  data object Failure : ChooseInsuranceUiState

  data object NotAllowed : ChooseInsuranceUiState
}

internal sealed interface ChooseInsuranceToCustomizeEvent {
  data class SelectInsurance(val insuranceId: String) :
    ChooseInsuranceToCustomizeEvent

  data object RetryLoadData : ChooseInsuranceToCustomizeEvent

  data class SubmitSelectedInsuranceToTerminate(val insurance: CustomisableInsurance) :
    ChooseInsuranceToCustomizeEvent

  data object ClearTerminationStep : ChooseInsuranceToCustomizeEvent
}
