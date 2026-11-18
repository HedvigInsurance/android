package com.hedvig.android.feature.editcoinsured.ui.triage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Redirect
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.public.restoredAsRedirect
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class EditCoInsuredTriageViewModel(
  getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
  @Assisted insuranceId: String?,
  @Assisted type: CoInsuredFlowType,
) : MoleculeViewModel<
    EditCoInsuredTriageEvent,
    EditCoInsuredTriageUiState,
  >(
    initialState = Loading,
    presenter = EditCoInsuredTriagePresenter(getInsuranceForEditCoInsuredUseCase, insuranceId, type),
  )

internal class EditCoInsuredTriagePresenter(
  private val getInsuranceForEditCoInsuredUseCase: GetInsurancesForEditCoInsuredUseCase,
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
    var currentState by remember {
      mutableStateOf(
        when (lastState) {
          // A taken redirect means this triage is still shown after its redirect, as when an equal key
          // elsewhere shares the ViewModel, so it loads again.
          is Redirect -> lastState.step.restoredAsRedirect()?.let(::Redirect) ?: Loading

          else -> lastState
        },
      )
    }
    var selected by remember {
      mutableStateOf((currentState as? Success)?.selected)
    }
    CollectEvents { event ->
      when (event) {
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
      if (currentState is Redirect) return@LaunchedEffect
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
          currentState = if (preselected != null) {
            Redirect(NavigationRequest.of(preselected))
          } else {
            Success(
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
      is Redirect -> currentStateValue
      is Success -> currentStateValue.copy(selected = selected)
    }
  }
}

internal sealed interface EditCoInsuredTriageEvent {
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

  /**
   * One insurance fits (the contract asked for, or the only one), so the picker is skipped for its step. Shown as
   * loading. Events are ignored and nothing loads again. A restart keeps a pending one, and loads again after a
   * taken one.
   */
  data class Redirect(val step: NavigationRequest<InsuranceForEditOrAddCoInsured>) : EditCoInsuredTriageUiState
}
