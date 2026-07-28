package com.hedvig.android.feature.onboarding.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.icon.CircleFilled
import com.hedvig.android.design.system.hedvig.icon.CircleOutline
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Settings
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
import com.hedvig.android.theme.Theme
import dev.zacsweers.metro.Inject
import hedvig.resources.ONBOARDING_CHANGE_SETTINGS_LATER_LABEL
import hedvig.resources.ONBOARDING_THEME_DARK_SUBTITLE
import hedvig.resources.ONBOARDING_THEME_LIGHT_SUBTITLE
import hedvig.resources.ONBOARDING_THEME_SUBTITLE
import hedvig.resources.ONBOARDING_THEME_SYSTEM_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.SETTINGS_THEME_DARK
import hedvig.resources.SETTINGS_THEME_DIALOG_TITLE
import hedvig.resources.SETTINGS_THEME_LIGHT
import hedvig.resources.SETTINGS_THEME_SYSTEM_DEFAULT
import hedvig.resources.general_continue_button
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingThemeViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  settingsDataStore: SettingsDataStore,
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

@Composable
internal fun OnboardingThemeDestination(viewModel: OnboardingThemeViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingThemeUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingThemeEvent.Close) },
  ) {
    when (uiState) {
      OnboardingThemeUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingThemeUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingThemeEvent.Retry) },
        )
      }

      is OnboardingThemeUiState.Content -> {
        val content = uiState as OnboardingThemeUiState.Content
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.SETTINGS_THEME_DIALOG_TITLE),
          description = stringResource(Res.string.ONBOARDING_THEME_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        RadioGroup(
          options = listOf(
            RadioOption(
              id = RadioOptionId(Theme.SYSTEM_DEFAULT.name),
              text = stringResource(Res.string.SETTINGS_THEME_SYSTEM_DEFAULT),
              label = stringResource(Res.string.ONBOARDING_THEME_SYSTEM_SUBTITLE),
              iconResource = IconResource.Vector(HedvigIcons.Settings),
            ),
            RadioOption(
              id = RadioOptionId(Theme.LIGHT.name),
              text = stringResource(Res.string.SETTINGS_THEME_LIGHT),
              label = stringResource(Res.string.ONBOARDING_THEME_LIGHT_SUBTITLE),
              iconResource = IconResource.Vector(HedvigIcons.CircleOutline),
            ),
            RadioOption(
              id = RadioOptionId(Theme.DARK.name),
              text = stringResource(Res.string.SETTINGS_THEME_DARK),
              label = stringResource(Res.string.ONBOARDING_THEME_DARK_SUBTITLE),
              iconResource = IconResource.Vector(HedvigIcons.CircleFilled),
            ),
          ),
          selectedOption = RadioOptionId(content.selectedTheme.name),
          onRadioOptionSelected = { viewModel.emit(OnboardingThemeEvent.SelectTheme(Theme.valueOf(it.id))) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        HedvigText(
          text = stringResource(Res.string.ONBOARDING_CHANGE_SETTINGS_LATER_LABEL),
          style = HedvigTheme.typography.finePrint,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.general_continue_button),
          onPrimaryClick = { viewModel.emit(OnboardingThemeEvent.Continue) },
        )
      }
    }
  }
}
