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
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceUiState.Failure
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
    initialState = Loading(),
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
          if (currentStateValue is Loading) {
            currentState = currentStateValue.copy(
              paramsToNavigateToNextStep = null,
            )
          }
        }
      }
    }

    LaunchedEffect(insuranceToFetchIntentFor) {
      val customisableInsurance = insuranceToFetchIntentFor ?: return@LaunchedEffect
      currentState = Loading()
      currentState = tierRepository
        .startChangeTierIntentAndGetQuotesId(customisableInsurance.id, SELF_SERVICE)
        .fold(
          ifLeft = {
            insuranceToFetchIntentFor = null
            Failure
          },
          ifRight = { intent ->
            val params = InsuranceCustomizationParameters(
              insuranceId = customisableInsurance.id,
              activationDate = intent.activationDate,
              quoteIds = intent.quotes.map { it.id },
            )
            insuranceToFetchIntentFor = null
            Loading(params)
          },
        )
    }

    LaunchedEffect(loadIteration) {
      if (lastState !is ChooseInsuranceUiState.Success) {
        currentState = ChooseInsuranceUiState.Loading()
      }
      getCustomizableInsurancesUseCase.invoke().collect { contractsResult ->
        contractsResult.fold(
          ifLeft = {
            logcat(priority = LogPriority.INFO) { "Cannot load contracts for changing tier-deductible" }
            currentState = ChooseInsuranceUiState.Failure
          },
          ifRight = { eligibleInsurances ->
            logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for changing tier-deductible" }
            if (eligibleInsurances == null) {
              currentState = ChooseInsuranceUiState.NotAllowed
            } else if (eligibleInsurances.size == 1) {
              insuranceToFetchIntentFor = eligibleInsurances[0]
            } else {
              val selectedInsurance = eligibleInsurances.firstOrNull { it.id == initialSelected }
              currentState = ChooseInsuranceUiState.Success(
                insuranceList = eligibleInsurances,
                selectedInsurance = selectedInsurance,
                changeTierIntentFailedToLoad = false,
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
  data class Loading(
    val paramsToNavigateToNextStep: InsuranceCustomizationParameters? = null,
  ) : ChooseInsuranceUiState

  data class Success(
    val insuranceList: List<CustomisableInsurance>,
    val selectedInsurance: CustomisableInsurance?,
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
