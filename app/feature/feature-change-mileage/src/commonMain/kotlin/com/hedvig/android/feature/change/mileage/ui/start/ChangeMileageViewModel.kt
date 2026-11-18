package com.hedvig.android.feature.change.mileage.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.feature.change.mileage.data.ChangeMileageIntent
import com.hedvig.android.feature.change.mileage.data.CreateMileageIntentError
import com.hedvig.android.feature.change.mileage.data.CreateMileageIntentUseCase
import com.hedvig.android.feature.change.mileage.data.GetMileageContractsUseCase
import com.hedvig.android.feature.change.mileage.data.MileageContract
import com.hedvig.android.feature.change.mileage.data.MileageQuote
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryParameters
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.datetime.LocalDate

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ChangeMileageViewModel(
  @Assisted contractId: String,
  getMileageContractsUseCase: GetMileageContractsUseCase,
  createMileageIntentUseCase: CreateMileageIntentUseCase,
  backstack: Backstack,
) : MoleculeViewModel<ChangeMileageEvent, ChangeMileageUiState>(
    initialState = ChangeMileageUiState.Loading,
    presenter = ChangeMileagePresenter(
      contractId = contractId,
      getMileageContractsUseCase = getMileageContractsUseCase,
      createMileageIntentUseCase = createMileageIntentUseCase,
      backstack = backstack,
    ),
  )

internal class ChangeMileagePresenter(
  private val contractId: String,
  private val getMileageContractsUseCase: GetMileageContractsUseCase,
  private val createMileageIntentUseCase: CreateMileageIntentUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<ChangeMileageEvent, ChangeMileageUiState> {
  @Composable
  override fun MoleculePresenterScope<ChangeMileageEvent>.present(
    lastState: ChangeMileageUiState,
  ): ChangeMileageUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        ChangeMileageEvent.Reload -> {
          loadIteration++
        }

        is ChangeMileageEvent.SelectQuote -> {
          val state = currentState as? ChangeMileageUiState.Success ?: return@CollectEvents
          currentState = state.copy(selectedQuoteId = event.quoteId)
        }

        ChangeMileageEvent.Continue -> {
          val state = currentState as? ChangeMileageUiState.Success ?: return@CollectEvents
          if (state.isCurrentSelected) return@CollectEvents
          backstack.add(
            ChangeMileageSummaryKey(
              ChangeMileageSummaryParameters(
                contractDisplayName = state.contract.displayName,
                contractGroup = state.contract.contractGroup,
                activationDate = state.activationDate,
                currentMonthlyCost = state.currentTotalCost.monthlyNet,
                quote = state.selectedQuote,
              ),
            ),
          )
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (loadIteration == 0 && lastState is ChangeMileageUiState.Success) return@LaunchedEffect
      currentState = ChangeMileageUiState.Loading
      val contract = getMileageContractsUseCase.invoke().fold(
        ifLeft = { null },
        ifRight = { contracts ->
          contracts.firstOrNull { it.id == contractId }.also {
            if (it == null) logcat(LogPriority.WARN) { "Contract $contractId does not support changing mileage" }
          }
        },
      )
      if (contract == null) {
        currentState = ChangeMileageUiState.Failure
        return@LaunchedEffect
      }
      createMileageIntentUseCase.invoke(contractId).fold(
        ifLeft = { error ->
          currentState = when (error) {
            CreateMileageIntentError.General -> ChangeMileageUiState.Failure
            is CreateMileageIntentError.UserError -> ChangeMileageUiState.UserError(error.message)
          }
        },
        ifRight = { intent ->
          currentState = intent.toSuccessState(contract)
        },
      )
    }

    return currentState
  }
}

private fun ChangeMileageIntent.toSuccessState(contract: MileageContract): ChangeMileageUiState {
  val currentQuote = quotes.firstOrNull { it.isCurrent }
  if (currentQuote == null) {
    logcat(LogPriority.ERROR) { "Change mileage intent for ${contract.id} has no current quote" }
    return ChangeMileageUiState.Failure
  }
  return ChangeMileageUiState.Success(
    contract = contract,
    activationDate = activationDate,
    currentTotalCost = currentTotalCost,
    quotes = quotes,
    selectedQuoteId = currentQuote.id,
  )
}

internal sealed interface ChangeMileageEvent {
  data class SelectQuote(val quoteId: String) : ChangeMileageEvent

  data object Continue : ChangeMileageEvent

  data object Reload : ChangeMileageEvent
}

internal sealed interface ChangeMileageUiState {
  data object Loading : ChangeMileageUiState

  data object Failure : ChangeMileageUiState

  data class UserError(val message: String) : ChangeMileageUiState

  data class Success(
    val contract: MileageContract,
    val activationDate: LocalDate,
    val currentTotalCost: ItemCost,
    val quotes: List<MileageQuote>,
    val selectedQuoteId: String,
  ) : ChangeMileageUiState {
    val selectedQuote: MileageQuote = quotes.first { it.id == selectedQuoteId }
    val isCurrentSelected: Boolean = selectedQuote.isCurrent
  }
}
