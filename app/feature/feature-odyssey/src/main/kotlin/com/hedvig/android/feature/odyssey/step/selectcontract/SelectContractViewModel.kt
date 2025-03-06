package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractUiState.Loading
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractUiState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import octopus.AllContractsQuery

internal class SelectContractViewModel(
  selectContract: ClaimFlowDestination.SelectContract,
  claimFlowRepository: ClaimFlowRepository,
  apolloClient: ApolloClient,
) : MoleculeViewModel<SelectContractEvent, SelectContractUiState>(
    initialState = Loading,
    presenter = SelectContractPresenter(
      selectContract = selectContract,
      claimFlowRepository = claimFlowRepository,
      apolloClient = apolloClient,
    ),
  )

private class SelectContractPresenter(
  private val selectContract: ClaimFlowDestination.SelectContract,
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

    LaunchedEffect(loadIteration) {
      currentState = Loading
      val result = apolloClient.query(AllContractsQuery())
        .safeExecute(::ErrorMessage).getOrNull()
      val allShortInfo = result.toAllContractsShortInfo()
      val initialOptions = selectContract.options.map { option ->
        ContractOptionForSelection(
          id = option.id,
          displayName = option.displayName,
          description = allShortInfo.firstOrNull { it.id == option.id }?.exposureName
            ?: "-",
        )
      }
      currentState = if (initialOptions.isEmpty()) {
        SelectContractUiState.Error
      } else {
        Success(
          contractOptions = initialOptions,
          selectedContract = initialOptions[0],
        )
      }
    }

    LaunchedEffect(submitIteration) {
      val state = currentState as? Success ?: return@LaunchedEffect
      if (!state.canSubmit || submitIteration < 1) return@LaunchedEffect
      currentState = state.copy(isButtonLoading = true)
      val selected = state.selectedContract
      claimFlowRepository.submitContract(selected.id).fold(
        ifLeft = {
          currentState = state.copy(error = true)
        },
        ifRight = { claimFlowStep ->
          currentState = state.copy(nextStep = claimFlowStep, isButtonLoading = false)
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SelectContractEvent.HandledNextStepNavigation -> {
          val state = currentState as? Success ?: return@CollectEvents
          currentState = state.copy(nextStep = null)
        }
        is SelectContractEvent.SelectContractOption -> {
          val state = currentState as? Success ?: return@CollectEvents
          val selected = state.contractOptions.first { it.id == event.selectedContractId }
          currentState = state.copy(selectedContract = selected)
        }

        SelectContractEvent.ShowedError -> {
          val state = currentState as? Success ?: return@CollectEvents
          currentState = state.copy(error = false)
        }
        SelectContractEvent.Submit -> {
          submitIteration++
        }

        SelectContractEvent.Reload -> loadIteration++
      }
    }
    return currentState
  }
}

private fun AllContractsQuery.Data?.toAllContractsShortInfo(): List<ShortContractInfo> {
  val activeContracts = this?.currentMember?.activeContracts?.map {
    ShortContractInfo(
      id = it.id,
      exposureName = it.exposureDisplayName,
    )
  } ?: listOf()
  val pendingContracts = this?.currentMember?.pendingContracts?.map {
    ShortContractInfo(
      id = it.id,
      exposureName = it.exposureDisplayName,
    )
  } ?: listOf()
  val terminatedContracts = this?.currentMember?.terminatedContracts?.map {
    ShortContractInfo(
      id = it.id,
      exposureName = it.exposureDisplayName,
    )
  } ?: listOf()
  return activeContracts + pendingContracts + terminatedContracts
}

internal sealed interface SelectContractUiState {
  data object Loading : SelectContractUiState

  data object Error : SelectContractUiState

  data class Success(
    val contractOptions: List<ContractOptionForSelection>,
    val selectedContract: ContractOptionForSelection,
    val isButtonLoading: Boolean = false,
    val error: Boolean = false,
    val nextStep: ClaimFlowStep? = null,
  ) : SelectContractUiState {
    val canSubmit: Boolean = nextStep == null
  }
}

internal data class ContractOptionForSelection(
  val id: String,
  val displayName: String,
  val description: String,
)

private data class ShortContractInfo(
  val id: String,
  val exposureName: String,
)

internal sealed interface SelectContractEvent {
  data class SelectContractOption(val selectedContractId: String) :
    SelectContractEvent

  data object Submit : SelectContractEvent

  data object Reload : SelectContractEvent

  data object HandledNextStepNavigation : SelectContractEvent

  data object ShowedError : SelectContractEvent
}
