package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.start.StartEvent.DismissStartError
import com.hedvig.android.feature.movingflow.ui.start.StartEvent.NavigatedToNextStep
import com.hedvig.android.feature.movingflow.ui.start.StartEvent.SelectHousingType
import com.hedvig.android.feature.movingflow.ui.start.StartEvent.SubmitHousingType
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import octopus.feature.movingflow.MoveIntentV2CreateMutation

internal class StartViewModel(
  apolloClient: ApolloClient,
  movingFlowRepository: MovingFlowRepository,
) : MoleculeViewModel<StartEvent, StartUiState>(
  StartUiState.Content(HousingType.entries, HousingType.entries.first(), null, null),
  StartPresenter(apolloClient, movingFlowRepository),
)

private class StartPresenter(
  private val apolloClient: ApolloClient,
  private val movingFlowRepository: MovingFlowRepository,
) : MoleculePresenter<StartEvent, StartUiState> {
  @Suppress("NAME_SHADOWING")
  @Composable
  override fun MoleculePresenterScope<StartEvent>.present(lastState: StartUiState): StartUiState {
    var selectedHousingType by remember {
      mutableStateOf((lastState as? StartUiState.Content)?.selectedHousingType ?: HousingType.entries.first())
    }
    var submittingHousingType: HousingType? by remember { mutableStateOf(null) }
    var error: StartError? by remember { mutableStateOf(null) }
    var moveIntentId: String? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is SelectHousingType -> selectedHousingType = event.housingType
        SubmitHousingType -> submittingHousingType = selectedHousingType
        NavigatedToNextStep -> moveIntentId = null
        DismissStartError -> error = null
      }
    }

    val submittingHousingTypeValue = submittingHousingType
    if (submittingHousingTypeValue != null) {
      LaunchedEffect(submittingHousingTypeValue) {
        either {
          val moveIntentCreate = apolloClient
            .mutation(MoveIntentV2CreateMutation())
            .safeExecute()
            .mapLeft(::ErrorMessage)
            .mapLeft { StartError.GenericError(it) }
            .map { it.moveIntentCreate }
            .bind()
          val moveIntent = ensureNotNull(moveIntentCreate.moveIntent) {
            val userError = moveIntentCreate.userError?.message
            if (userError == null) {
              StartUiState.StartError.GenericError(ErrorMessage("Unknown MoveIntentV2CreateMutation error"))
            } else {
              StartUiState.StartError.UserPresentable(userError)
            }
          }
          movingFlowRepository.initiateNewMovingFlow(moveIntent, submittingHousingTypeValue)
          moveIntent.id
        }.fold(
          ifLeft = {
            Snapshot.withMutableSnapshot {
              error = it
              submittingHousingType = null
            }
          },
          ifRight = { id ->
            Snapshot.withMutableSnapshot {
              moveIntentId = id
              submittingHousingType = null
            }
          },
        )
      }
    }
    error?.let { error ->
      return error
    }
    return StartUiState.Content(
      possibleHousingTypes = HousingType.entries,
      selectedHousingType = selectedHousingType,
      submittingHousingType = submittingHousingType,
      initiatedMovingFlowId = moveIntentId,
    )
  }
}

internal sealed interface StartEvent {
  data class SelectHousingType(val housingType: HousingType) : StartEvent

  data object SubmitHousingType : StartEvent

  data object NavigatedToNextStep : StartEvent

  data object DismissStartError : StartEvent
}

internal sealed interface StartUiState {
  sealed interface StartError : StartUiState {
    data class UserPresentable(val message: String) : StartError

    data class GenericError(val errorMessage: ErrorMessage) : StartError, ErrorMessage by errorMessage
  }

  data class Content(
    val possibleHousingTypes: List<HousingType>,
    val selectedHousingType: HousingType,
    val submittingHousingType: HousingType?,
    val initiatedMovingFlowId: String?,
  ) : StartUiState {
    val isLoading: Boolean = submittingHousingType != null || initiatedMovingFlowId != null
  }
}
