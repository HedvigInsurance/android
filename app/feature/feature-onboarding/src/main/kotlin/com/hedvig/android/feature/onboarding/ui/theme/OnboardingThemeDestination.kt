package com.hedvig.android.feature.onboarding.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
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
  OnboardingThemeDestination(
    uiState = uiState,
    navigateUp = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingThemeEvent.Close) },
    onRetry = { viewModel.emit(OnboardingThemeEvent.Retry) },
    onSelectTheme = { viewModel.emit(OnboardingThemeEvent.SelectTheme(it)) },
    onContinueClick = { viewModel.emit(OnboardingThemeEvent.Continue) },
  )
}

@Composable
private fun OnboardingThemeDestination(
  uiState: OnboardingThemeUiState,
  navigateUp: () -> Unit,
  onCloseClick: () -> Unit,
  onRetry: () -> Unit,
  onSelectTheme: (Theme) -> Unit,
  onContinueClick: () -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingThemeUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onCloseClick,
  ) {
    when (uiState) {
      OnboardingThemeUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingThemeUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingThemeUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.SETTINGS_THEME_DIALOG_TITLE),
          description = stringResource(Res.string.ONBOARDING_THEME_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          ThemeOptionRow(
            theme = Theme.SYSTEM_DEFAULT,
            title = stringResource(Res.string.SETTINGS_THEME_SYSTEM_DEFAULT),
            description = stringResource(Res.string.ONBOARDING_THEME_SYSTEM_SUBTITLE),
            selected = uiState.selectedTheme == Theme.SYSTEM_DEFAULT,
            onClick = { onSelectTheme(Theme.SYSTEM_DEFAULT) },
          )
          ThemeOptionRow(
            theme = Theme.LIGHT,
            title = stringResource(Res.string.SETTINGS_THEME_LIGHT),
            description = stringResource(Res.string.ONBOARDING_THEME_LIGHT_SUBTITLE),
            selected = uiState.selectedTheme == Theme.LIGHT,
            onClick = { onSelectTheme(Theme.LIGHT) },
          )
          ThemeOptionRow(
            theme = Theme.DARK,
            title = stringResource(Res.string.SETTINGS_THEME_DARK),
            description = stringResource(Res.string.ONBOARDING_THEME_DARK_SUBTITLE),
            selected = uiState.selectedTheme == Theme.DARK,
            onClick = { onSelectTheme(Theme.DARK) },
          )
        }
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
          onPrimaryClick = onContinueClick,
        )
      }
    }
  }
}

@Composable
private fun ThemeOptionRow(
  theme: Theme,
  title: String,
  description: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
    modifier = modifier
      .fillMaxWidth()
      .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.padding(start = 12.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
      Box(
        Modifier
          .clip(HedvigTheme.shapes.cornerSmall)
          .background(HedvigTheme.colorScheme.surfaceSecondaryTransparent)
          .padding(8.dp),
      ) {
        ThemeOptionIcon(theme)
      }
      Column(Modifier.weight(1f)) {
        HedvigText(text = title, style = HedvigTheme.typography.bodySmall)
        HedvigText(
          text = description,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      ThemeSelectionIndicator(selected)
    }
  }
}

@Composable
private fun ThemeOptionIcon(theme: Theme) {
  when (theme) {
    Theme.SYSTEM_DEFAULT -> Image(
      imageVector = HedvigIcons.Settings,
      contentDescription = null,
      colorFilter = ColorFilter.tint(HedvigTheme.colorScheme.fillPrimary),
      modifier = Modifier.size(24.dp),
    )

    Theme.LIGHT -> ThemeColorCircle(
      fillColor = LightThemeSwatchColor,
      borderColor = HedvigTheme.colorScheme.borderSecondary,
    )

    Theme.DARK -> ThemeColorCircle(
      fillColor = DarkThemeSwatchColor,
      borderColor = null,
    )
  }
}

@Composable
private fun ThemeColorCircle(fillColor: Color, borderColor: Color?) {
  Canvas(Modifier.size(24.dp)) {
    val radius = 19.dp.toPx() / 2f
    drawCircle(color = fillColor, radius = radius)
    if (borderColor != null) {
      val strokeWidth = 1.dp.toPx()
      drawCircle(color = borderColor, radius = radius - strokeWidth / 2f, style = Stroke(strokeWidth))
    }
  }
}

@Composable
private fun ThemeSelectionIndicator(selected: Boolean) {
  val color = if (selected) HedvigTheme.colorScheme.signalGreenElement else HedvigTheme.colorScheme.borderSecondary
  Canvas(Modifier.size(24.dp)) {
    val strokeWidth = if (selected) 8.dp.toPx() else 2.dp.toPx()
    drawCircle(color = color, radius = size.minDimension / 2f - strokeWidth / 2f, style = Stroke(strokeWidth))
  }
}

// Fixed light/dark swatches: they preview the theme itself, so they stay constant regardless of the active theme.
private val LightThemeSwatchColor = Color(0xFFFAFAFA)
private val DarkThemeSwatchColor = Color(0xFF121212)

@HedvigPreview
@Composable
private fun PreviewOnboardingThemeDestination() {
  HedvigTheme {
    OnboardingThemeDestination(
      uiState = OnboardingThemeUiState.Content(
        progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
        selectedTheme = Theme.SYSTEM_DEFAULT,
      ),
      navigateUp = {},
      onCloseClick = {},
      onRetry = {},
      onSelectTheme = {},
      onContinueClick = {},
    )
  }
}
