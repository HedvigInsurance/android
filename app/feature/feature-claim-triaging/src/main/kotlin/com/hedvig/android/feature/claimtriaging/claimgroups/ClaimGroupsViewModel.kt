package com.hedvig.android.feature.claimtriaging.claimgroups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.launch

internal class ClaimGroupsViewModel(
  getEntryPointGroupsUseCase: GetEntryPointGroupsUseCase,
  claimFlowRepository: ClaimFlowRepository,
) : MoleculeViewModel<ClaimGroupsEvent, ClaimGroupsUiState>(
    initialState = ClaimGroupsUiState(),
    presenter = ClaimGroupsPresenter(getEntryPointGroupsUseCase::invoke, claimFlowRepository),
  )

internal class ClaimGroupsPresenter(
  private val getEntryPointGroups: suspend () -> Either<ErrorMessage, List<ClaimGroup>>,
  private val claimFlowRepository: ClaimFlowRepository,
) : MoleculePresenter<ClaimGroupsEvent, ClaimGroupsUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimGroupsEvent>.present(lastState: ClaimGroupsUiState): ClaimGroupsUiState {
    var uiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    // Auto-load claim groups on first launch or when retry is requested
    LaunchedEffect(loadIteration) {
      // If we already have claim groups loaded (state preservation), skip loading
      if (lastState.claimGroups.isNotEmpty() && loadIteration == 0) {
        return@LaunchedEffect
      }

      uiState = uiState.copy(isLoading = true)
      getEntryPointGroups().fold(
        ifLeft = { errorMessage ->
          logcat(LogPriority.INFO, errorMessage.throwable) {
            "ClaimGroupsViewModel failed to load entry groups"
          }
          uiState = uiState.copy(
            chipLoadingErrorMessage = errorMessage.message,
            isLoading = false,
          )
        },
        ifRight = { claimGroups ->
          uiState = uiState.copy(
            claimGroups = claimGroups,
            selectedClaimGroup = null,
            chipLoadingErrorMessage = null,
            isLoading = false,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        ClaimGroupsEvent.LoadClaimGroups -> {
          loadIteration++
        }

        ClaimGroupsEvent.ContinueWithoutSelection -> {
          uiState = uiState.copy(haveTriedContinuingWithoutSelection = true)
        }

        is ClaimGroupsEvent.SelectClaimGroup -> {
          uiState = uiState.copy(
            selectedClaimGroup = event.claimGroup,
            haveTriedContinuingWithoutSelection = false,
          )
        }

        ClaimGroupsEvent.StartClaimFlow -> {
          if (uiState.isLoading) return@CollectEvents
          uiState = uiState.copy(isLoading = true)
          launch {
            claimFlowRepository.startClaimFlow(null, null).fold(
              ifLeft = { errorMessage ->
                uiState = uiState.copy(
                  isLoading = false,
                  startClaimErrorMessage = errorMessage.message,
                )
              },
              ifRight = { claimFlowStep ->
                uiState = uiState.copy(
                  isLoading = false,
                  nextStep = claimFlowStep,
                )
              },
            )
          }
        }

        ClaimGroupsEvent.DismissStartClaimError -> {
          uiState = uiState.copy(startClaimErrorMessage = null)
        }

        ClaimGroupsEvent.HandledNextStepNavigation -> {
          uiState = uiState.copy(nextStep = null)
        }
      }
    }

    return uiState
  }
}

internal sealed interface ClaimGroupsEvent {
  data object LoadClaimGroups : ClaimGroupsEvent

  data object ContinueWithoutSelection : ClaimGroupsEvent

  data class SelectClaimGroup(val claimGroup: ClaimGroup) : ClaimGroupsEvent

  data object StartClaimFlow : ClaimGroupsEvent

  data object DismissStartClaimError : ClaimGroupsEvent

  data object HandledNextStepNavigation : ClaimGroupsEvent
}

@Immutable
internal data class ClaimGroupsUiState(
  val claimGroups: List<ClaimGroup> = listOf(),
  val selectedClaimGroup: ClaimGroup? = null,
  val haveTriedContinuingWithoutSelection: Boolean = false,
  val chipLoadingErrorMessage: String? = null,
  val startClaimErrorMessage: String? = null,
  val isLoading: Boolean = true,
  val nextStep: ClaimFlowStep? = null,
) {
  val canContinue: Boolean
    get() = isLoading == false &&
      chipLoadingErrorMessage == null &&
      startClaimErrorMessage == null &&
      nextStep == null
}
