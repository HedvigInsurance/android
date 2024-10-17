package com.hedvig.android.feature.help.center.choosecoinsured

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredEvent.ClearNavigation
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredEvent.OnContinueWithSelected
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredEvent.Reload
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredEvent.SelectInsurance
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Failure
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Loading
import com.hedvig.android.feature.help.center.choosecoinsured.ChooseInsuranceForEditCoInsuredUiState.Success
import com.hedvig.android.feature.help.center.data.GetInsuranceForEditCoInsuredUseCase
import com.hedvig.android.feature.help.center.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseInsuranceForEditCoInsuredViewModel(
  getInsuranceForEditCoInsuredUseCase: GetInsuranceForEditCoInsuredUseCase,
) : MoleculeViewModel<
    ChooseInsuranceForEditCoInsuredEvent,
    ChooseInsuranceForEditCoInsuredUiState,
  >(
    initialState = Loading,
    presenter = ChooseInsuranceForEditCoInsuredPresenter(getInsuranceForEditCoInsuredUseCase),
  )

internal class ChooseInsuranceForEditCoInsuredPresenter(
  private val getInsuranceForEditCoInsuredUseCase: GetInsuranceForEditCoInsuredUseCase,
) : MoleculePresenter<
    ChooseInsuranceForEditCoInsuredEvent,
    ChooseInsuranceForEditCoInsuredUiState,
  > {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceForEditCoInsuredEvent>.present(
    lastState: ChooseInsuranceForEditCoInsuredUiState,
  ): ChooseInsuranceForEditCoInsuredUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var selected by remember { mutableStateOf((currentState as? Success)?.selected) }
    CollectEvents { event ->
      when (event) {
        is OnContinueWithSelected -> {
          val currentStateValue = currentState as? Success ?: return@CollectEvents
          currentState = currentStateValue.copy(destinationToNavigateToNextStep = selected?.quickLinkDestination)
        }
        Reload -> loadIteration++
        ClearNavigation -> {
          val currentStateValue = currentState as? Success ?: return@CollectEvents
          currentState = currentStateValue.copy(destinationToNavigateToNextStep = null)
        }
        is SelectInsurance -> {
          val currentStateValue = currentState as? Success ?: return@CollectEvents
          val selectedInsurance = currentStateValue.list.first { it.id == event.id }
          selected = selectedInsurance
        }
      }
    }
    LaunchedEffect(loadIteration) {
      currentState = Loading
      getInsuranceForEditCoInsuredUseCase.invoke().fold(
        ifLeft = {
          currentState = Failure
        },
        ifRight = { data ->
          currentState = Success(
            list = data,
            selected = null,
          )
        },
      )
    }
    val currentStateValue = currentState
    return when (currentStateValue) {
      Failure -> Failure
      Loading -> Loading
      is Success -> currentStateValue.copy(selected = selected)
    }
  }
}

internal sealed interface ChooseInsuranceForEditCoInsuredEvent {
  data object OnContinueWithSelected : ChooseInsuranceForEditCoInsuredEvent

  data object Reload : ChooseInsuranceForEditCoInsuredEvent

  data class SelectInsurance(val id: String) : ChooseInsuranceForEditCoInsuredEvent

  data object ClearNavigation : ChooseInsuranceForEditCoInsuredEvent
}

internal sealed interface ChooseInsuranceForEditCoInsuredUiState {
  data object Loading : ChooseInsuranceForEditCoInsuredUiState

  data object Failure : ChooseInsuranceForEditCoInsuredUiState

  data class Success(
    val list: List<InsuranceForEditOrAddCoInsured>,
    val selected: InsuranceForEditOrAddCoInsured?,
    val destinationToNavigateToNextStep: QuickLinkDestination.OuterDestination? = null,
  ) : ChooseInsuranceForEditCoInsuredUiState
}
