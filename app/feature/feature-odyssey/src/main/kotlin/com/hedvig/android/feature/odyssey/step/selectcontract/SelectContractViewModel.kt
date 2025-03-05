package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private class SelectContractPresenter(
  selectContract: ClaimFlowDestination.SelectContract,
  private val claimFlowRepository: ClaimFlowRepository,
  private val apolloClient: ApolloClient,
) : MoleculePresenter<SelectContractEvent, SelectContractUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectContractEvent>.present(
    lastState: SelectContractUiState,
  ): SelectContractUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }
    var contracts by remember {
      mutableStateOf<List<ContractOptionForSelection>>(listOf())
    }

    LaunchedEffect(loadIteration) {

    }

    LaunchedEffect(submitIteration) {
      if (!currentState.canSubmit || submitIteration < 1) return@LaunchedEffect
      currentState = currentState.copy(isLoading = true)
      val selected = currentState.selectedContract
      claimFlowRepository.submitContract(selected.id).fold(
        ifLeft = {
          currentState = currentState.copy(isLoading = false, error = true)
        },
        ifRight = { claimFlowStep ->
          currentState = currentState.copy(isLoading = false, nextStep = claimFlowStep)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SelectContractEvent.HandledNextStepNavigation -> currentState = currentState.copy(nextStep = null)
        is SelectContractEvent.SelectContractOption -> {
          val oldUiState = currentState
          val selected = oldUiState.contractOptions.first { it.id == event.selectedContractId }
          oldUiState.copy(selectedContract = selected)
        }

        SelectContractEvent.ShowedError -> currentState = currentState.copy(error = false)
        SelectContractEvent.Submit -> {
          submitIteration++
        }
      }
    }
    return currentState
  }
}


internal class SelectContractViewModel(
  selectContract: ClaimFlowDestination.SelectContract,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    SelectContractUiState
      .fromInitialSelection(selectContract.options),
  )
  val uiState: StateFlow<SelectContractUiState> = _uiState.asStateFlow()

  fun selectContractOption(selectedContractId: String) {
    _uiState.update { oldUiState ->
      val selected = oldUiState.contractOptions.first { it.id == selectedContractId }
      oldUiState.copy(selectedContract = selected)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(error = false)
    }
  }

  fun submitContract() {
    val uiState = _uiState.value
    val selectedContract = uiState.selectedContract
    if (selectedContract == null || !uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitContract(selectedContract.id).fold(
        ifLeft = {
          _uiState.update {
            it.copy(isLoading = false, error = true)
          }
        },
        ifRight = { claimFlowStep ->
          _uiState.update {
            it.copy(isLoading = false, nextStep = claimFlowStep)
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class SelectContractUiState(
  val contractOptions: List<ContractOptionForSelection>,
  val selectedContract: ContractOptionForSelection,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(locationOptions: List<ContractOptionForSelection>): SelectContractUiState {
      return SelectContractUiState(
        contractOptions = locationOptions,
        selectedContract = locationOptions.first(),
      )
    }
  }
}

internal data class ContractOptionForSelection(
  val id: String,
  val displayName: String,
  val description: String,
)

internal sealed interface SelectContractEvent {
  data class SelectContractOption(val selectedContractId: String) :
    SelectContractEvent

  data object Submit : SelectContractEvent

  data object HandledNextStepNavigation : SelectContractEvent

  data object ShowedError : SelectContractEvent
}
