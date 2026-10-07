package com.hedvig.android.ui.analytics.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

/**
 * A consent screen's answer as its presenter sees it: what is stored, and the member's decision while
 * it is being applied. Held by [rememberAnalyticsConsentDecision].
 */
@Stable
class AnalyticsConsentDecision internal constructor(
  storedConsent: State<AnalyticsConsent?>,
) {
  /** The stored consent, `null` until it has been read. */
  val consent: AnalyticsConsent? by storedConsent

  /** The badge to show on [AnalyticsConsentCard], `null` also while [consent] is still unread. */
  val badge: ConsentBadge?
    get() = consent?.let(ConsentBadge::from)

  internal var pending: AnalyticsConsent? by mutableStateOf(null)

  /** A decision is being applied. Further decisions are ignored until it is done. */
  val isDeciding: Boolean
    get() = pending != null

  internal val settledBadges = MutableSharedFlow<ConsentBadge?>(replay = 1)

  fun decide(consent: AnalyticsConsent) {
    if (pending == null) pending = consent
  }

  /** Forward [AnalyticsConsentCard]'s `onBadgeSettled` here. */
  fun onBadgeSettled(badge: ConsentBadge?) {
    settledBadges.tryEmit(badge)
  }
}

/**
 * Stores each [AnalyticsConsentDecision.decide] and then calls [onDecided], once [AnalyticsConsentCard]
 * has finished showing the answer, so the member sees it land before the screen moves on.
 *
 * [lastConsent] is the consent the screen last showed, so a restarted presenter keeps showing it rather
 * than briefly treating it as unread.
 */
@Composable
fun rememberAnalyticsConsentDecision(
  settingsDataStore: SettingsDataStore,
  lastConsent: AnalyticsConsent?,
  onDecided: suspend () -> Unit,
): AnalyticsConsentDecision {
  val storedConsent = remember(settingsDataStore) {
    settingsDataStore.observeAnalyticsConsent()
  }.collectAsState(lastConsent)
  val decision = remember(storedConsent) { AnalyticsConsentDecision(storedConsent) }
  val currentOnDecided by rememberUpdatedState(onDecided)
  LaunchedEffect(decision, decision.pending) {
    val consent = decision.pending ?: return@LaunchedEffect
    val shownBadge = decision.badge
    settingsDataStore.setAnalyticsConsent(consent)
    val answeredBadge = ConsentBadge.from(consent)
    // An unchanged badge never animates, so the card has nothing new to report.
    if (answeredBadge != shownBadge) {
      decision.settledBadges.first { settledBadge -> settledBadge == answeredBadge }
    }
    currentOnDecided()
    // Released rather than kept, for a screen that stays on the back stack and is interactive again
    // when the member comes back to it.
    decision.pending = null
  }
  return decision
}
