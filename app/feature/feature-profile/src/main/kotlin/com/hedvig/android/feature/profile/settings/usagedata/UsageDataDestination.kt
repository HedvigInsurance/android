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
import androidx.compose.runtime.collectAsState
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
import com.hedvig.android.ui.analytics.consent.AnalyticsConsentCard
import com.hedvig.android.ui.analytics.consent.ConsentBadge
import dev.zacsweers.metro.Inject
import hedvig.resources.LEGAL_PRIVACY_POLICY_APP_SHORT
import hedvig.resources.ONBOARDING_ANALYTICS_ALLOW_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_DENY_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_SUBTITLE
import hedvig.resources.ONBOARDING_ANALYTICS_TITLE
import hedvig.resources.Res
import hedvig.resources.SETTINGS_USAGE_DATA_TITLE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
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
    val storedConsent by remember { settingsDataStore.observeAnalyticsConsent() }.collectAsState(lastState.consent)
    var decision by remember { mutableStateOf<AnalyticsConsent?>(null) }
    var finished by remember { mutableStateOf(lastState.finished) }
    val badgeSettleSignals = remember { MutableSharedFlow<ConsentBadge?>(replay = 1) }

    // Leaves once the card has finished showing the answer, so the member sees it land before the pop.
    LaunchedEffect(decision) {
      val consent = decision ?: return@LaunchedEffect
      settingsDataStore.setAnalyticsConsent(consent)
      badgeSettleSignals.first { settledBadge -> settledBadge == ConsentBadge.from(consent) }
      finished = true
    }

    CollectEvents { event ->
      when (event) {
        UsageDataEvent.Allow -> if (decision == null) decision = AnalyticsConsent.GRANTED
        UsageDataEvent.Deny -> if (decision == null) decision = AnalyticsConsent.DENIED
        is UsageDataEvent.BadgeSettled -> badgeSettleSignals.tryEmit(event.badge)
      }
    }
    return UsageDataUiState(
      consent = storedConsent,
      buttonsEnabled = decision == null,
      finished = finished,
    )
  }
}

internal data class UsageDataUiState(
  /** `null` until the stored consent has been read. */
  val consent: AnalyticsConsent? = null,
  val buttonsEnabled: Boolean = true,
  val finished: Boolean = false,
)

internal sealed interface UsageDataEvent {
  data object Allow : UsageDataEvent

  data object Deny : UsageDataEvent

  data class BadgeSettled(val badge: ConsentBadge?) : UsageDataEvent
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
    uiState = uiState,
    navigateUp = navigateUp,
    onBadgeSettled = { badge -> viewModel.emit(UsageDataEvent.BadgeSettled(badge)) },
    onAllow = { viewModel.emit(UsageDataEvent.Allow) },
    onDeny = { viewModel.emit(UsageDataEvent.Deny) },
    onPrivacyPolicy = onPrivacyPolicy,
  )
}

@Composable
private fun UsageDataScreen(
  uiState: UsageDataUiState,
  navigateUp: () -> Unit,
  onBadgeSettled: (ConsentBadge?) -> Unit,
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
    Spacer(Modifier.height(24.dp))
    val consent = uiState.consent
    if (consent != null) {
      AnalyticsConsentCard(
        badge = ConsentBadge.from(consent),
        onBadgeSettled = onBadgeSettled,
        modifier = Modifier.align(Alignment.CenterHorizontally),
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(24.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .clip(CircleShape)
        .clickable(onClick = onPrivacyPolicy)
        .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
      HedvigText(
        text = stringResource(Res.string.LEGAL_PRIVACY_POLICY_APP_SHORT),
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
      enabled = uiState.buttonsEnabled,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(Res.string.ONBOARDING_ANALYTICS_DENY_BUTTON),
      onClick = onDeny,
      enabled = uiState.buttonsEnabled,
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
      UsageDataScreen(
        uiState = UsageDataUiState(consent = AnalyticsConsent.GRANTED),
        navigateUp = {},
        onBadgeSettled = {},
        onAllow = {},
        onDeny = {},
        onPrivacyPolicy = {},
      )
    }
  }
}
