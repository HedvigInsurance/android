package com.hedvig.android.feature.greencard.ui.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ChooseContractForGreenCardViewModel(
  getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
) : MoleculeViewModel<ChooseContractForGreenCardEvent, ChooseContractForGreenCardUiState>(
    initialState = ChooseContractForGreenCardUiState.Loading,
    presenter = ChooseContractForGreenCardPresenter(getGreenCardSpecificationsUseCase),
  )

internal class ChooseContractForGreenCardPresenter(
  private val getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
) : MoleculePresenter<ChooseContractForGreenCardEvent, ChooseContractForGreenCardUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseContractForGreenCardEvent>.present(
    lastState: ChooseContractForGreenCardUiState,
  ): ChooseContractForGreenCardUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        ChooseContractForGreenCardEvent.RetryLoadData -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is ChooseContractForGreenCardUiState.Success) return@LaunchedEffect
      currentState = ChooseContractForGreenCardUiState.Loading
      currentState = getGreenCardSpecificationsUseCase.invoke().fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Could not load contracts eligible for green cards: $it" }
          ChooseContractForGreenCardUiState.Failure
        },
        ifRight = { ChooseContractForGreenCardUiState.Success(it.contracts) },
      )
    }
    return currentState
  }
}

internal sealed interface ChooseContractForGreenCardEvent {
  data object RetryLoadData : ChooseContractForGreenCardEvent
}

internal sealed interface ChooseContractForGreenCardUiState {
  data object Loading : ChooseContractForGreenCardUiState

  data object Failure : ChooseContractForGreenCardUiState

  data class Success(val contracts: List<GreenCardContractSpecification>) : ChooseContractForGreenCardUiState
}
