package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.Loading
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.fragment.MoveIntentFragment

internal class StartViewModel(
  apolloClient: ApolloClient,
  movingFlowRepository: MovingFlowRepository,
) : MoleculeViewModel<StartEvent, StartUiState>(
    StartUiState.Loading,
    StartPresenter(apolloClient, movingFlowRepository),
  )

internal class StartPresenter(
  private val apolloClient: ApolloClient,
  private val movingFlowRepository: MovingFlowRepository,
) : MoleculePresenter<StartEvent, StartUiState> {
  @Suppress("NAME_SHADOWING")
  @Composable
  override fun MoleculePresenterScope<StartEvent>.present(lastState: StartUiState): StartUiState {
    var submittingHousingType: HousingType? by remember { mutableStateOf(null) }
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is SelectHousingType -> {
          val state = currentState as? StartUiState.Content ?: return@CollectEvents
          currentState = state.copy(selectedHousingType = event.housingType)
        }
        SubmitHousingType -> {
          val state = currentState as? StartUiState.Content ?: return@CollectEvents
          submittingHousingType = state.selectedHousingType
        }
        NavigatedToNextStep -> {
          val state = currentState as? StartUiState.Content ?: return@CollectEvents
          currentState = state.copy(navigateToNextStep = false)
        }
        DismissStartError -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(loadIteration) {
      either {
        if (loadIteration > 0) {
          currentState = Loading
        }
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
            StartError.GenericError(ErrorMessage("Unknown MoveIntentV2CreateMutation error"))
          } else {
            StartError.UserPresentable(userError)
          }
        }
        moveIntent
      }.fold(
        ifLeft = {
          Snapshot.withMutableSnapshot {
            currentState = it
            submittingHousingType = null
          }
        },
        ifRight = { intent ->
          Snapshot.withMutableSnapshot {
            currentState = StartUiState.Content(
              possibleHousingTypes = HousingType.entries,
              selectedHousingType = HousingType.entries.first(),
              initiatedMovingIntent = intent,
              oldHomeInsuranceDuration = intent.currentHomeAddresses.first().oldAddressCoverageDurationDays,
              navigateToNextStep = false,
            )
            submittingHousingType = null
          }
        },
      )
    }

    val submittingHousingTypeValue = submittingHousingType
    if (submittingHousingTypeValue != null) {
      LaunchedEffect(submittingHousingTypeValue) {
        val state = currentState as? StartUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(buttonLoading = true)
        val moveIntent = state.initiatedMovingIntent
        movingFlowRepository.initiateNewMovingFlow(moveIntent, submittingHousingTypeValue)
        submittingHousingType = null
        currentState = state.copy(navigateToNextStep = true, buttonLoading = false)
      }
    }
    return currentState
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

  data object Loading : StartUiState

  data class Content(
    val possibleHousingTypes: List<HousingType>,
    val selectedHousingType: HousingType,
    val initiatedMovingIntent: MoveIntentFragment,
    val oldHomeInsuranceDuration: Int?,
    val navigateToNextStep: Boolean,
    val buttonLoading: Boolean = false,
  ) : StartUiState
}
