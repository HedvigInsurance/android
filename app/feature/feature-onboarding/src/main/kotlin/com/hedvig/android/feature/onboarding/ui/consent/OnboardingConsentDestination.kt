package com.hedvig.android.feature.onboarding.ui.consent

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigTextButton
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
internal class OnboardingConsentViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  settingsDataStore: SettingsDataStore,
) : MoleculeViewModel<OnboardingConsentEvent, OnboardingConsentUiState>(
    initialState = OnboardingConsentUiState.Loading,
    presenter = OnboardingConsentPresenter(sessionStore, navigator, settingsDataStore),
  )

internal class OnboardingConsentPresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val settingsDataStore: SettingsDataStore,
) : MoleculePresenter<OnboardingConsentEvent, OnboardingConsentUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingConsentEvent>.present(
    lastState: OnboardingConsentUiState,
  ): OnboardingConsentUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingConsentUiState.Content) return@LaunchedEffect
      currentState = OnboardingConsentUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingConsentUiState.Error },
        ifRight = { session ->
          currentState = OnboardingConsentUiState.Content(session.progressFor(OnboardingStepId.AnalyticsConsent))
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingConsentEvent.Retry -> loadIteration++

        OnboardingConsentEvent.Close -> launch { navigator.exitOnboarding() }

        OnboardingConsentEvent.Allow -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.GRANTED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }

        OnboardingConsentEvent.Deny -> launch {
          settingsDataStore.setAnalyticsConsent(AnalyticsConsent.DENIED)
          navigator.continueFrom(OnboardingStepId.AnalyticsConsent)
        }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingConsentUiState {
  data object Loading : OnboardingConsentUiState

  data object Error : OnboardingConsentUiState

  data class Content(val progress: OnboardingProgress) : OnboardingConsentUiState
}

internal sealed interface OnboardingConsentEvent {
  data object Retry : OnboardingConsentEvent

  data object Close : OnboardingConsentEvent

  data object Allow : OnboardingConsentEvent

  data object Deny : OnboardingConsentEvent
}

@Composable
internal fun OnboardingConsentDestination(
  viewModel: OnboardingConsentViewModel,
  navigateUp: () -> Unit,
  openPrivacyPolicy: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingConsentUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingConsentEvent.Close) },
  ) {
    when (uiState) {
      OnboardingConsentUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingConsentUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingConsentEvent.Retry) },
        )
      }

      is OnboardingConsentUiState.Content -> {
        Spacer(Modifier.height(8.dp))
        OnboardingStepHeader(
          // TODO: Add "Help us make the app better" / "Hjälp oss göra appen bättre" to Lokalise
          title = "Help us make the app better",
          // TODO: Add the body copy below (and its Swedish translation) to Lokalise
          description = "We use technical tools to see how you use the app, so we can make it better.\n\n" +
            "Product analytics is completely optional and can be turned off any time in settings. " +
            "This data is never used for marketing.",
        )
        Spacer(Modifier.weight(1f))
        // TODO: Add "Privacy policy" / "Integritetspolicy" to Lokalise
        HedvigTextButton(
          text = "Privacy policy",
          onClick = openPrivacyPolicy,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        OnboardingStepButtons(
          // TODO: Add "Allow" / "Tillåt" to Lokalise
          primaryText = "Allow",
          onPrimaryClick = { viewModel.emit(OnboardingConsentEvent.Allow) },
          // TODO: Add "Deny" / "Neka" to Lokalise
          secondaryText = "Deny",
          onSecondaryClick = { viewModel.emit(OnboardingConsentEvent.Deny) },
        )
      }
    }
  }
}
