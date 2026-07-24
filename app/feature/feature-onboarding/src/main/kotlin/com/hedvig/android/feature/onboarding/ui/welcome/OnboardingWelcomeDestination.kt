package com.hedvig.android.feature.onboarding.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingWelcomeViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
) : MoleculeViewModel<OnboardingWelcomeEvent, OnboardingWelcomeUiState>(
    initialState = OnboardingWelcomeUiState.Loading,
    presenter = OnboardingWelcomePresenter(sessionStore, navigator),
  )

internal class OnboardingWelcomePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingWelcomeEvent, OnboardingWelcomeUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingWelcomeEvent>.present(
    lastState: OnboardingWelcomeUiState,
  ): OnboardingWelcomeUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingWelcomeUiState.Content) return@LaunchedEffect
      currentState = OnboardingWelcomeUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingWelcomeUiState.Error },
        ifRight = { session ->
          currentState = OnboardingWelcomeUiState.Content(session.progressFor(null))
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingWelcomeEvent.Retry -> loadIteration++
        OnboardingWelcomeEvent.GetStarted -> launch { navigator.continueFrom(null) }
        OnboardingWelcomeEvent.Close -> launch { navigator.exitOnboarding() }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingWelcomeUiState {
  data object Loading : OnboardingWelcomeUiState

  data object Error : OnboardingWelcomeUiState

  data class Content(val progress: OnboardingProgress) : OnboardingWelcomeUiState
}

internal sealed interface OnboardingWelcomeEvent {
  data object Retry : OnboardingWelcomeEvent

  data object GetStarted : OnboardingWelcomeEvent

  data object Close : OnboardingWelcomeEvent
}

@Composable
internal fun OnboardingWelcomeDestination(viewModel: OnboardingWelcomeViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  // System back here pops the flow root natively (predictive back preview included); marking the
  // flow as seen on that dismissal is handled by OnboardingGate.markSeenWhenOnboardingDismissed.
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingWelcomeUiState.Content)?.progress,
    showBackButton = false,
    onBackClick = {},
    onCloseClick = { viewModel.emit(OnboardingWelcomeEvent.Close) },
  ) {
    when (uiState) {
      OnboardingWelcomeUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingWelcomeUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingWelcomeEvent.Retry) },
        )
      }

      is OnboardingWelcomeUiState.Content -> {
        Spacer(Modifier.weight(1f))
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          // Static stand-in for the Figma app-icon-with-badge graphic; animation is a follow-up.
          Box(
            Modifier
              .size(96.dp)
              .background(HedvigTheme.colorScheme.fillPrimary, HedvigTheme.shapes.cornerLarge),
          )
          Spacer(Modifier.height(24.dp))
          // TODO: Add "Welcome to Hedvig" / "Välkommen till Hedvig" to Lokalise
          HedvigText(
            text = "Welcome to Hedvig",
            textAlign = TextAlign.Center,
          )
          // TODO: Add "Follow the steps to get started with your insurance" /
          //  "Följ stegen för att komma igång med din försäkring" to Lokalise
          HedvigText(
            text = "Follow the steps to get started with your insurance",
            color = HedvigTheme.colorScheme.textSecondary,
            textAlign = TextAlign.Center,
          )
        }
        Spacer(Modifier.weight(1f))
        // TODO: Add "Get started" / "Kom igång" to Lokalise
        OnboardingStepButtons(
          primaryText = "Get started",
          onPrimaryClick = { viewModel.emit(OnboardingWelcomeEvent.GetStarted) },
        )
      }
    }
  }
}
