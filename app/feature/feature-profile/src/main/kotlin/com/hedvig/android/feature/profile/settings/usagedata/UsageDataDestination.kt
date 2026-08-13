package com.hedvig.android.feature.profile.settings.usagedata

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import hedvig.resources.LEGAL_PRIVACY_POLICY
import hedvig.resources.ONBOARDING_ANALYTICS_ALLOW_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_DENY_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_SUBTITLE
import hedvig.resources.ONBOARDING_ANALYTICS_TITLE
import hedvig.resources.Res
import hedvig.resources.SETTINGS_USAGE_DATA_TITLE
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class UsageDataViewModel(
  settingsDataStore: SettingsDataStore,
) : MoleculeViewModel<UsageDataEvent, UsageDataUiState>(
    initialState = UsageDataUiState(),
    presenter = UsageDataPresenter(settingsDataStore),
  )

internal class UsageDataPresenter(
  private val settingsDataStore: SettingsDataStore,
) : MoleculePresenter<UsageDataEvent, UsageDataUiState> {
  @Composable
  override fun MoleculePresenterScope<UsageDataEvent>.present(lastState: UsageDataUiState): UsageDataUiState {
    var finished by remember { mutableStateOf(lastState.finished) }
    CollectEvents { event ->
      val consent = when (event) {
        UsageDataEvent.Allow -> AnalyticsConsent.GRANTED
        UsageDataEvent.Deny -> AnalyticsConsent.DENIED
      }
      launch {
        settingsDataStore.setAnalyticsConsent(consent)
        finished = true
      }
    }
    return UsageDataUiState(finished = finished)
  }
}

internal data class UsageDataUiState(val finished: Boolean = false)

internal sealed interface UsageDataEvent {
  data object Allow : UsageDataEvent

  data object Deny : UsageDataEvent
}

@Composable
internal fun UsageDataDestination(
  viewModel: UsageDataViewModel,
  navigateUp: () -> Unit,
  popBackstack: () -> Unit,
  onPrivacyPolicy: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.finished) {
    if (uiState.finished) popBackstack()
  }
  UsageDataScreen(
    navigateUp = navigateUp,
    onAllow = { viewModel.emit(UsageDataEvent.Allow) },
    onDeny = { viewModel.emit(UsageDataEvent.Deny) },
    onPrivacyPolicy = onPrivacyPolicy,
  )
}

@Composable
private fun UsageDataScreen(
  navigateUp: () -> Unit,
  onAllow: () -> Unit,
  onDeny: () -> Unit,
  onPrivacyPolicy: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.SETTINGS_USAGE_DATA_TITLE),
    navigateUp = navigateUp,
  ) {
    Spacer(Modifier.height(8.dp))
    Column(Modifier.padding(horizontal = 16.dp)) {
      HedvigText(text = stringResource(Res.string.ONBOARDING_ANALYTICS_TITLE))
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = stringResource(Res.string.ONBOARDING_ANALYTICS_SUBTITLE),
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
    Spacer(Modifier.weight(1f))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .clip(CircleShape)
        .clickable(onClick = onPrivacyPolicy)
        .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
      HedvigText(
        text = stringResource(Res.string.LEGAL_PRIVACY_POLICY),
        style = HedvigTheme.typography.bodySmall,
        textDecoration = TextDecoration.Underline,
      )
      Icon(
        imageVector = HedvigIcons.ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.ONBOARDING_ANALYTICS_ALLOW_BUTTON),
      onClick = onAllow,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(Res.string.ONBOARDING_ANALYTICS_DENY_BUTTON),
      onClick = onDeny,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewUsageDataScreen() {
  HedvigTheme {
    Surface {
      UsageDataScreen(navigateUp = {}, onAllow = {}, onDeny = {}, onPrivacyPolicy = {})
    }
  }
}
