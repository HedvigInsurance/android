package com.hedvig.android.feature.onboarding.ui.petid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingPetIdViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingPetIdEvent, OnboardingPetIdUiState>(
    initialState = OnboardingPetIdUiState.Loading,
    presenter = OnboardingPetIdPresenter(sessionStore, navigator),
  )

internal class OnboardingPetIdPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingPetIdEvent, OnboardingPetIdUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingPetIdEvent>.present(
    lastState: OnboardingPetIdUiState,
  ): OnboardingPetIdUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    // Pin which contracts belong to this step the first time data is available, so completed
    // rows render as done instead of disappearing.
    var pinnedContractIds by remember { mutableStateOf<List<String>?>(null) }

    fun contentFrom(session: OnboardingSession): OnboardingPetIdUiState.Content {
      val ids = pinnedContractIds
        ?: session.data.contractsWithMissingPetId.map { it.id }.also { pinnedContractIds = it }
      return OnboardingPetIdUiState.Content(
        progress = session.progressFor(OnboardingStepId.PetIds),
        rows = ids.mapNotNull { id ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          PetIdRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            typeOfContract = contract.typeOfContract,
            isComplete = !contract.isMissingPetId,
          )
        },
      )
    }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingPetIdUiState.Content) return@LaunchedEffect
      currentState = OnboardingPetIdUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingPetIdUiState.Error },
        ifRight = { session -> currentState = contentFrom(session) },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingPetIdEvent.Retry -> loadIteration++

        OnboardingPetIdEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingPetIdEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed -> currentState = contentFrom(refreshed) }
          // onLeft: keep the previously shown state, per the spec's error handling.
        }

        OnboardingPetIdEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.PetIds) }
      }
    }

    return currentState
  }
}

internal data class PetIdRow(
  val contractId: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val isComplete: Boolean,
)

internal sealed interface OnboardingPetIdUiState {
  data object Loading : OnboardingPetIdUiState

  data object Error : OnboardingPetIdUiState

  data class Content(
    val progress: OnboardingProgress,
    val rows: List<PetIdRow>,
  ) : OnboardingPetIdUiState
}

internal sealed interface OnboardingPetIdEvent {
  data object Retry : OnboardingPetIdEvent

  data object Close : OnboardingPetIdEvent

  data object Refresh : OnboardingPetIdEvent

  data object Continue : OnboardingPetIdEvent
}
