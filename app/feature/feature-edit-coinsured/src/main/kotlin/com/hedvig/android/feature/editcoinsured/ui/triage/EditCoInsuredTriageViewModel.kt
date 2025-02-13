package com.hedvig.android.feature.editcoinsured.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.OnContinueWithSelected
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class EditCoInsuredTriageViewModel(
  getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
  insuranceId: String?,
) : MoleculeViewModel<
    EditCoInsuredTriageEvent,
    EditCoInsuredTriageUiState,
  >(
    initialState = Loading,
    presenter = EditCoInsuredTriagePresenter(getInsuranceForEditCoInsuredUseCase, insuranceId),
  )

internal class EditCoInsuredTriagePresenter(
  private val getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
  private val insuranceId: String?,
) : MoleculePresenter<
    EditCoInsuredTriageEvent,
    EditCoInsuredTriageUiState,
  > {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredTriageEvent>.present(
    lastState: EditCoInsuredTriageUiState,
  ): EditCoInsuredTriageUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var selected by remember {
      mutableStateOf((currentState as? Success)?.selected)
    }
    CollectEvents { event ->
      when (event) {
        is OnContinueWithSelected -> {
          val currentStateValue = currentState as? Success ?: return@CollectEvents
          selected?.let {
            currentState = when (it.destination) {
              EditCoInsuredDestination.MISSING_INFO -> currentStateValue.copy(
                idToNavigateToAddMissingInfo = it.id,
              )
              EditCoInsuredDestination.ADD_OR_REMOVE -> currentStateValue.copy(
                idToNavigateToAddOrRemoveCoInsured = it.id,
              )
            }
          }
        }
        EditCoInsuredTriageEvent.Reload -> loadIteration++
        EditCoInsuredTriageEvent.ClearNavigation -> {
          val currentStateValue = currentState as? Success ?: return@CollectEvents
          currentState = currentStateValue.copy(
            idToNavigateToAddMissingInfo = null,
            idToNavigateToAddOrRemoveCoInsured = null,
          )
        }
        is EditCoInsuredTriageEvent.SelectInsurance -> {
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
          val preselected = if (insuranceId != null) {
            data.firstOrNull { it.id == insuranceId }
          } else if (data.size == 1) {
            data[0]
          } else {
            null
          }
          val success = Success(
            list = data,
            selected = preselected,
            idToNavigateToAddMissingInfo =
              if (preselected?.destination == EditCoInsuredDestination.MISSING_INFO) preselected.id else null,
            idToNavigateToAddOrRemoveCoInsured =
              if (preselected?.destination == EditCoInsuredDestination.ADD_OR_REMOVE) preselected.id else null,
          )
          currentState = success
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

internal sealed interface EditCoInsuredTriageEvent {
  data object OnContinueWithSelected : EditCoInsuredTriageEvent

  data object Reload : EditCoInsuredTriageEvent

  data class SelectInsurance(val id: String) : EditCoInsuredTriageEvent

  data object ClearNavigation : EditCoInsuredTriageEvent
}

internal sealed interface EditCoInsuredTriageUiState {
  data object Loading : EditCoInsuredTriageUiState

  data object Failure : EditCoInsuredTriageUiState

  data class Success(
    val list: List<InsuranceForEditOrAddCoInsured>,
    val selected: InsuranceForEditOrAddCoInsured?,
    val idToNavigateToAddMissingInfo: String? = null,
    val idToNavigateToAddOrRemoveCoInsured: String? = null,
  ) : EditCoInsuredTriageUiState
}
