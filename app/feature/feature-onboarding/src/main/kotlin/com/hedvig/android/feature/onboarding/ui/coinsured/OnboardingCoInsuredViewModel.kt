package com.hedvig.android.feature.onboarding.ui.coinsured

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.coinsured.CoInsuredFlowType
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
internal class OnboardingCoInsuredViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingCoInsuredEvent, OnboardingCoInsuredUiState>(
    initialState = OnboardingCoInsuredUiState.Loading,
    presenter = OnboardingCoInsuredPresenter(sessionStore, navigator),
  )

internal class OnboardingCoInsuredPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingCoInsuredEvent, OnboardingCoInsuredUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingCoInsuredEvent>.present(
    lastState: OnboardingCoInsuredUiState,
  ): OnboardingCoInsuredUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    fun contentFrom(session: OnboardingSession): OnboardingCoInsuredUiState.Content {
      val pinned = session.pinnedCoInsuredContracts
      return OnboardingCoInsuredUiState.Content(
        progress = session.progressFor(OnboardingStepId.CoInsured),
        rows = pinned.mapNotNull { (id, flowType) ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          CoInsuredRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            typeOfContract = contract.typeOfContract,
            flowType = flowType,
            isComplete = contract.coInsuredFlowType == null,
          )
        },
      )
    }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingCoInsuredUiState.Content) return@LaunchedEffect
      currentState = OnboardingCoInsuredUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingCoInsuredUiState.Error },
        ifRight = { session -> currentState = contentFrom(session) },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingCoInsuredEvent.Retry -> loadIteration++

        OnboardingCoInsuredEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingCoInsuredEvent.Refresh -> launch {
          sessionStore.refreshData().onRight { refreshed -> currentState = contentFrom(refreshed) }
          // onLeft: keep the previously shown state, per the spec's error handling.
        }

        is OnboardingCoInsuredEvent.AddCoInsured -> navigator.openAddCoInsured(event.contractId, event.flowType)

        OnboardingCoInsuredEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.CoInsured) }
      }
    }

    return currentState
  }
}

internal data class CoInsuredRow(
  val contractId: String,
  val displayName: String,
  val exposureName: String,
  val typeOfContract: String,
  val flowType: CoInsuredFlowType,
  val isComplete: Boolean,
)

internal sealed interface OnboardingCoInsuredUiState {
  data object Loading : OnboardingCoInsuredUiState

  data object Error : OnboardingCoInsuredUiState

  data class Content(
    val progress: OnboardingProgress,
    val rows: List<CoInsuredRow>,
  ) : OnboardingCoInsuredUiState
}

internal sealed interface OnboardingCoInsuredEvent {
  data object Retry : OnboardingCoInsuredEvent

  data object Close : OnboardingCoInsuredEvent

  data object Refresh : OnboardingCoInsuredEvent

  data class AddCoInsured(val contractId: String, val flowType: CoInsuredFlowType) : OnboardingCoInsuredEvent

  data object Continue : OnboardingCoInsuredEvent
}
