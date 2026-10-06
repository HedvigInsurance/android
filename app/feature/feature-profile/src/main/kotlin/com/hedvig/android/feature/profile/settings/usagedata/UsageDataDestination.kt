package com.hedvig.android.feature.profile.settings.usagedata

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.ui.analytics.consent.AnalyticsConsentContent
import com.hedvig.android.ui.analytics.consent.ConsentBadge
import com.hedvig.android.ui.analytics.consent.rememberAnalyticsConsentDecision
import dev.zacsweers.metro.Inject
import hedvig.resources.Res
import hedvig.resources.SETTINGS_USAGE_DATA_TITLE
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
    val decision = rememberAnalyticsConsentDecision(
      settingsDataStore = settingsDataStore,
      lastConsent = lastState.consent,
      onDecided = { finished = true },
    )
    CollectEvents { event ->
      when (event) {
        UsageDataEvent.Allow -> decision.decide(AnalyticsConsent.GRANTED)
        UsageDataEvent.Deny -> decision.decide(AnalyticsConsent.DENIED)
        is UsageDataEvent.BadgeSettled -> decision.onBadgeSettled(event.badge)
      }
    }
    return UsageDataUiState(
      consent = decision.consent,
      buttonsEnabled = !decision.isDeciding,
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
    val consent = uiState.consent ?: return@HedvigScaffold
    Spacer(Modifier.height(8.dp))
    AnalyticsConsentContent(
      badge = ConsentBadge.from(consent),
      buttonsEnabled = uiState.buttonsEnabled,
      onBadgeSettled = onBadgeSettled,
      onAllow = onAllow,
      onDeny = onDeny,
      onPrivacyPolicy = onPrivacyPolicy,
    )
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
