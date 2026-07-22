package com.hedvig.android.feature.onboarding.ui.coinsured

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
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

    // Pin which contracts belong to this step the first time data is available, so completed
    // rows render as done instead of disappearing.
    var pinnedContractIds by remember { mutableStateOf<List<String>?>(null) }

    fun contentFrom(session: OnboardingSession): OnboardingCoInsuredUiState.Content {
      val ids = pinnedContractIds
        ?: session.data.contractsWithMissingCoInsured.map { it.id }.also { pinnedContractIds = it }
      return OnboardingCoInsuredUiState.Content(
        progress = session.progressFor(OnboardingStepId.CoInsured),
        rows = ids.mapNotNull { id ->
          val contract = session.data.contracts.firstOrNull { it.id == id } ?: return@mapNotNull null
          CoInsuredRow(
            contractId = contract.id,
            displayName = contract.displayName,
            exposureName = contract.exposureName,
            isComplete = contract.missingCoInsuredCount == 0,
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

        is OnboardingCoInsuredEvent.AddCoInsured -> navigator.openAddCoInsured(event.contractId)

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

  data class AddCoInsured(val contractId: String) : OnboardingCoInsuredEvent

  data object Continue : OnboardingCoInsuredEvent
}

@Composable
internal fun OnboardingCoInsuredDestination(viewModel: OnboardingCoInsuredViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(OnboardingCoInsuredEvent.Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }

  OnboardingStepScaffold(
    progress = (uiState as? OnboardingCoInsuredUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingCoInsuredEvent.Close) },
  ) {
    when (val content = uiState) {
      OnboardingCoInsuredUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingCoInsuredUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingCoInsuredEvent.Retry) },
        )
      }

      is OnboardingCoInsuredUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          // TODO: Add "Add co-insured" / "Lägg till medförsäkrade" to Lokalise
          title = "Add co-insured",
          // TODO: Add "So we know who's covered by your insurance" / "Så att vi vet vem som är försäkrad" to Lokalise
          description = "So we know who's covered by your insurance",
        )
        Spacer(Modifier.height(16.dp))
        for (row in content.rows) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          ) {
            Column(Modifier.weight(1f)) {
              HedvigText(row.displayName)
              HedvigText(row.exposureName, color = HedvigTheme.colorScheme.textSecondary)
            }
            if (row.isComplete) {
              Icon(imageVector = HedvigIcons.Checkmark, contentDescription = null)
            } else {
              HedvigButton(
                // TODO: Add "Add" / "Lägg till" to Lokalise
                text = "Add",
                onClick = { viewModel.emit(OnboardingCoInsuredEvent.AddCoInsured(row.contractId)) },
                enabled = true,
                buttonSize = ButtonDefaults.ButtonSize.Small,
              )
            }
          }
        }
        Spacer(Modifier.weight(1f))
        HedvigText(
          // TODO: Add "You can add this information later" / "Du kan lägga till den här informationen senare" to Lokalise
          text = "You can add this information later",
          style = HedvigTheme.typography.finePrint,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        OnboardingStepButtons(
          // TODO: Add "Continue" / "Fortsätt" to Lokalise
          primaryText = "Continue",
          onPrimaryClick = { viewModel.emit(OnboardingCoInsuredEvent.Continue) },
          // TODO: Add "Do this later" / "Gör det senare" to Lokalise
          secondaryText = "Do this later",
          onSecondaryClick = { viewModel.emit(OnboardingCoInsuredEvent.Continue) },
        )
      }
    }
  }
}
