package com.hedvig.android.feature.onboarding.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
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
import hedvig.resources.Res
import hedvig.resources.SETTINGS_THEME_DARK
import hedvig.resources.SETTINGS_THEME_LIGHT
import hedvig.resources.SETTINGS_THEME_SYSTEM_DEFAULT
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
          // TODO: Add "Choose theme" / "Välj tema" to Lokalise
          title = "Choose theme",
          // TODO: Add "Customize the look of the app" / "Anpassa utseendet på appen" to Lokalise
          description = "Customize the look of the app",
        )
        Spacer(Modifier.height(16.dp))
        ThemeOptionRow(
          title = stringResource(Res.string.SETTINGS_THEME_SYSTEM_DEFAULT),
          // TODO: Add "Uses your phone's setting" / "Använder din telefons inställning" to Lokalise
          subtitle = "Uses your phone's setting",
          isSelected = content.selectedTheme == Theme.SYSTEM_DEFAULT,
          onClick = { viewModel.emit(OnboardingThemeEvent.SelectTheme(Theme.SYSTEM_DEFAULT)) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        ThemeOptionRow(
          title = stringResource(Res.string.SETTINGS_THEME_LIGHT),
          // TODO: Add "Set light mode" / "Ange ljust läge" to Lokalise
          subtitle = "Set light mode",
          isSelected = content.selectedTheme == Theme.LIGHT,
          onClick = { viewModel.emit(OnboardingThemeEvent.SelectTheme(Theme.LIGHT)) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        ThemeOptionRow(
          title = stringResource(Res.string.SETTINGS_THEME_DARK),
          // TODO: Add "Set dark mode" / "Ange mörkt läge" to Lokalise
          subtitle = "Set dark mode",
          isSelected = content.selectedTheme == Theme.DARK,
          onClick = { viewModel.emit(OnboardingThemeEvent.SelectTheme(Theme.DARK)) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        HedvigText(
          // TODO: Add "You can change these settings later" / "Du kan ändra dessa inställningar senare" to Lokalise
          text = "You can change these settings later",
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
          onPrimaryClick = { viewModel.emit(OnboardingThemeEvent.Continue) },
        )
      }
    }
  }
}

@Composable
private fun ThemeOptionRow(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clip(HedvigTheme.shapes.cornerMedium)
      .clickable(onClick = onClick)
      .padding(vertical = 16.dp),
  ) {
    Column(Modifier.weight(1f)) {
      HedvigText(title)
      HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
    }
    if (isSelected) {
      Icon(imageVector = HedvigIcons.Checkmark, contentDescription = null)
    }
  }
}
