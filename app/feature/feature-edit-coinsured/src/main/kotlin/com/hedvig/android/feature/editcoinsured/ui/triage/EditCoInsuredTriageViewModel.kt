package com.hedvig.android.feature.editcoinsured.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.navigateFromTriage
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.OnContinueWithSelected
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel
internal class EditCoInsuredTriageViewModel(
  getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
  backstack: Backstack,
  @Assisted insuranceId: String?,
  @Assisted type: CoInsuredFlowType,
) : MoleculeViewModel<
    EditCoInsuredTriageEvent,
    EditCoInsuredTriageUiState,
  >(
    initialState = Loading,
    presenter = EditCoInsuredTriagePresenter(getInsuranceForEditCoInsuredUseCase, backstack, insuranceId, type),
  )

internal class EditCoInsuredTriagePresenter(
  private val getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
  private val backstack: Backstack,
  private val insuranceId: String?,
  private val type: CoInsuredFlowType,
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
          currentState as? Success ?: return@CollectEvents
          selected?.let { backstack.navigateFromTriage(it.toNavKey()) }
        }

        EditCoInsuredTriageEvent.Reload -> {
          loadIteration++
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
      getInsuranceForEditCoInsuredUseCase.invoke(type).fold(
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
          if (preselected != null) {
            // Single match (deep link or only one insurance): skip the picker and navigate directly,
            // popping the triage entry so back leaves the flow.
            backstack.navigateFromTriage(preselected.toNavKey())
          } else {
            currentState = Success(
              list = data,
              selected = null,
              type = type,
            )
          }
        },
      )
    }
    return when (val currentStateValue = currentState) {
      Failure -> Failure
      Loading -> Loading
      is Success -> currentStateValue.copy(selected = selected)
    }
  }
}

private fun InsuranceForEditOrAddCoInsured.toNavKey(): HedvigNavKey = when (destination) {
  EditCoInsuredDestination.MISSING_INFO -> CoInsuredAddInfoKey(id, type)
  EditCoInsuredDestination.ADD_OR_REMOVE -> CoInsuredAddOrRemoveKey(id, type)
}

internal sealed interface EditCoInsuredTriageEvent {
  data object OnContinueWithSelected : EditCoInsuredTriageEvent

  data object Reload : EditCoInsuredTriageEvent

  data class SelectInsurance(val id: String) : EditCoInsuredTriageEvent
}

internal sealed interface EditCoInsuredTriageUiState {
  data object Loading : EditCoInsuredTriageUiState

  data object Failure : EditCoInsuredTriageUiState

  data class Success(
    val list: List<InsuranceForEditOrAddCoInsured>,
    val selected: InsuranceForEditOrAddCoInsured?,
    val type: CoInsuredFlowType,
  ) : EditCoInsuredTriageUiState
}
