package com.hedvig.android.feature.onboarding.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.theme.Theme
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingThemeViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  settingsDataStore: SettingsDataStore,
  val progressBarAnimation: OnboardingProgressBarAnimation,
) : MoleculeViewModel<OnboardingThemeEvent, OnboardingThemeUiState>(
    initialState = OnboardingThemeUiState.Loading,
    presenter = OnboardingThemePresenter(sessionStore, navigator, settingsDataStore),
  )

internal class OnboardingThemePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
  private val settingsDataStore: SettingsDataStore,
) : MoleculePresenter<OnboardingThemeEvent, OnboardingThemeUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingThemeEvent>.present(
    lastState: OnboardingThemeUiState,
  ): OnboardingThemeUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    val storedTheme = settingsDataStore.observeTheme().collectAsState(null).value

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingThemeUiState.Content) return@LaunchedEffect
      currentState = OnboardingThemeUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingThemeUiState.Error },
        ifRight = { session ->
          currentState = OnboardingThemeUiState.Content(
            progress = session.progressFor(OnboardingStepId.Theme),
            // Placeholder; the return site merges the live stored theme.
            selectedTheme = Theme.SYSTEM_DEFAULT,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingThemeEvent.Retry -> loadIteration++
        OnboardingThemeEvent.Close -> launch { navigator.exitOnboarding() }
        is OnboardingThemeEvent.SelectTheme -> launch { settingsDataStore.setTheme(event.theme) }
        OnboardingThemeEvent.Continue -> launch { navigator.continueFrom(OnboardingStepId.Theme) }
      }
    }

    return if (currentState is OnboardingThemeUiState.Content) {
      (currentState as OnboardingThemeUiState.Content).copy(
        selectedTheme = storedTheme ?: Theme.SYSTEM_DEFAULT,
      )
    } else {
      currentState
    }
  }
}

internal sealed interface OnboardingThemeUiState {
  data object Loading : OnboardingThemeUiState

  data object Error : OnboardingThemeUiState

  data class Content(
    val progress: OnboardingProgress,
    val selectedTheme: Theme,
  ) : OnboardingThemeUiState
}

internal sealed interface OnboardingThemeEvent {
  data object Retry : OnboardingThemeEvent

  data object Close : OnboardingThemeEvent

  data class SelectTheme(val theme: Theme) : OnboardingThemeEvent

  data object Continue : OnboardingThemeEvent
}
