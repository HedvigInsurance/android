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
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.movingflow.EnterNewAddressKey
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.DismissStartError
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.SelectHousingType
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeEvent.SubmitHousingType
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.collectLatest

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class HousingTypeViewModel(
  @Assisted moveIntentId: String,
  movingFlowRepository: MovingFlowRepository,
  backstack: Backstack,
) : MoleculeViewModel<HousingTypeEvent, HousingTypeUiState>(
    HousingTypeUiState.Loading,
    HousingTypePresenter(moveIntentId, movingFlowRepository, backstack),
  )

private class HousingTypePresenter(
  private val moveIntentId: String,
  private val movingFlowRepository: MovingFlowRepository,
  private val backstack: Backstack,
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

        DismissStartError -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowRepository
        .movingFlowState()
        .collectLatest { movingFlowState ->
          Snapshot.withMutableSnapshot {
            currentState = HousingTypeUiState.Content(
              possibleHousingTypes = HousingType.entries,
              selectedHousingType = (currentState as? HousingTypeUiState.Content)?.selectedHousingType
                ?: movingFlowState?.housingType
                ?: HousingType.entries.first(),
            )
          }
        }
    }

    val submittingHousingTypeValue = submittingHousingType
    LaunchedEffect(submittingHousingTypeValue) {
      if (submittingHousingTypeValue != null) {
        movingFlowRepository.updateWithHousingType(submittingHousingTypeValue)
        backstack.add(EnterNewAddressKey(moveIntentId))
        submittingHousingType = null
      }
    }
    return when (val state = currentState) {
      is HousingTypeUiState.Content -> state.copy(buttonLoading = submittingHousingTypeValue != null)
      else -> state
    }
  }
}

internal sealed interface HousingTypeEvent {
  data class SelectHousingType(val housingType: HousingType) : HousingTypeEvent

  data object SubmitHousingType : HousingTypeEvent

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
    val buttonLoading: Boolean = false,
  ) : HousingTypeUiState
}
