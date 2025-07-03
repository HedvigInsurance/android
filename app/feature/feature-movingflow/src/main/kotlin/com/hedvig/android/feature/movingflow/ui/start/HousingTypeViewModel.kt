package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.DismissStartError
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.NavigatedToNextStep
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.SelectHousingType
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.SubmitHousingType
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest

internal class HousingTypeViewModel(
  movingFlowRepository: MovingFlowRepository,
) : MoleculeViewModel<HousingTypeEvent, HousingTypeUiState>(
    HousingTypeUiState.Loading,
    HousingTypePresenter(movingFlowRepository),
  )

private class HousingTypePresenter(
  private val movingFlowRepository: MovingFlowRepository,
) : MoleculePresenter<HousingTypeEvent, HousingTypeUiState> {
  @Suppress("NAME_SHADOWING")
  @Composable
  override fun MoleculePresenterScope<HousingTypeEvent>.present(lastState: HousingTypeUiState): HousingTypeUiState {
    var submittingHousingType: HousingType? by remember { mutableStateOf(null) }
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is SelectHousingType -> {
          val state = currentState as? HousingTypeUiState.Content ?: return@CollectEvents
          currentState = state.copy(selectedHousingType = event.housingType)
        }

        SubmitHousingType -> {
          val state = currentState as? HousingTypeUiState.Content ?: return@CollectEvents
          submittingHousingType = state.selectedHousingType
        }

        NavigatedToNextStep -> {
          val state = currentState as? HousingTypeUiState.Content ?: return@CollectEvents
          currentState = state.copy(navigateToNextStep = false)
        }

        DismissStartError -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowRepository
        .movingFlowState()
        .collectLatest {
          Snapshot.withMutableSnapshot {
            currentState = HousingTypeUiState.Content(
              possibleHousingTypes = HousingType.entries,
              selectedHousingType = HousingType.entries.first(),
              navigateToNextStep = false,
            )
            submittingHousingType = null
          }
        }
    }

    val submittingHousingTypeValue = submittingHousingType
    LaunchedEffect(submittingHousingTypeValue) {
      if (submittingHousingTypeValue != null) {
        val state = currentState as? HousingTypeUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(buttonLoading = true)
        movingFlowRepository.updateWithHousingType(submittingHousingTypeValue)
        submittingHousingType = null
        currentState = state.copy(navigateToNextStep = true, buttonLoading = false)
      }
    }
    return currentState
  }
}

internal sealed interface HousingTypeEvent {
  data class SelectHousingType(val housingType: HousingType) : HousingTypeEvent

  data object SubmitHousingType : HousingTypeEvent

  data object NavigatedToNextStep : HousingTypeEvent

  data object DismissStartError : HousingTypeEvent
}

internal sealed interface HousingTypeUiState {
  sealed interface HousingTypeError : HousingTypeUiState {
    data class UserPresentable(val message: String) : HousingTypeError

    data class GenericError(val errorMessage: ErrorMessage) : HousingTypeError, ErrorMessage by errorMessage
  }

  data object Loading : HousingTypeUiState

  data class Content(
    val possibleHousingTypes: List<HousingType>,
    val selectedHousingType: HousingType,
    val navigateToNextStep: Boolean,
    val buttonLoading: Boolean = false,
  ) : HousingTypeUiState
}
