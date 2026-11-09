package com.hedvig.android.app

import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.StashedSession
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.merge
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

/**
 * The single seam between an Activity lifecycle and the app-scoped [BackstackController]. Owns every
 * way the navigation state crosses that seam, so the decision about what the stack should be at
 * launch is made in one place rather than spread across `MainActivity`:
 *
 *  - the process-death snapshot serialized into the Activity's `SavedStateRegistry` ([NavStateSnapshot]),
 *  - the launch-time seeding precedence and the save-provider registration ([restoreAndPersist]).
 */
internal object NavigationStateBridge {
  private const val NAV_STATE_REGISTRY_KEY = "com.hedvig.android.app.NAV_STATE"

  /**
   * How long a [BackstackController.pendingDeepLink] may survive a process death and still be landed at
   * the next login. Covers a real "killed mid-login" window (switching to BankID, OTP, etc., is seconds
   * to a few minutes) while discarding a link stashed long ago, which would otherwise bleed into an
   * unrelated later login. See [restoreAndPersist].
   */
  private val MAX_PENDING_DEEP_LINK_AGE = 30.minutes

  /**
   * Seeds / restores the hoisted navigation state, then registers the provider that persists it.
   * The seeding precedence, read top to bottom:
   *  1. On a cold start after process death, re-hydrate the full state from the Activity's
   *     `SavedStateRegistry`. On a config change the live singleton is already populated, so
   *     [BackstackController.restoreFromSavedState] is a no-op and the live state wins.
   *  2. Finally guarantee at least a Login root.
   */
  fun restoreAndPersist(
    backstackController: BackstackController,
    savedStateRegistry: SavedStateRegistry,
    serializersModules: Set<SerializersModule>,
    clock: Clock,
  ) {
    val savedStateConfiguration = SavedStateConfiguration {
      this.serializersModule = serializersModules.merge()
    }

    val snapshot = savedStateRegistry.consumeRestoredStateForKey(NAV_STATE_REGISTRY_KEY)
      ?.let { decodeFromSavedState(NavStateSnapshot.serializer(), it, savedStateConfiguration) }
    if (snapshot != null) {
      // Discard a pending deep link that is too old to still belong to an in-flight login, so it
      // can't bleed into an unrelated later login.
      val now = clock.now().toEpochMilliseconds()
      val stashedAt = snapshot.pendingDeepLinkStashedAtEpochMs
      val pendingStillValid = snapshot.pendingDeepLink != null &&
        isPendingDeepLinkStashTimeFresh(stashedAt, now)
      backstackController.restoreFromSavedState(
        entries = snapshot.entries,
        parkedRuns = snapshot.parkedRuns,
        pendingDeepLink = snapshot.pendingDeepLink.takeIf { pendingStillValid },
        pendingDeepLinkStashedAtEpochMs = stashedAt.takeIf { pendingStillValid },
        stashedSession = snapshot.stashedSession,
      )
    }
    backstackController.seedIfEmpty(listOf(LoginKey))

    // Persist the live navigation state across process death. The provider is invoked at save time
    // and serializes whatever the singleton holds then, so a Presenter-driven navigation is captured.
    savedStateRegistry.registerSavedStateProvider(NAV_STATE_REGISTRY_KEY) {
      encodeToSavedState(
        NavStateSnapshot.serializer(),
        NavStateSnapshot(
          entries = backstackController.entries.toList(),
          parkedRuns = backstackController.parkedRuns.toMap(),
          pendingDeepLink = backstackController.pendingDeepLink,
          pendingDeepLinkStashedAtEpochMs = backstackController.pendingDeepLinkStashedAtEpochMs,
          stashedSession = backstackController.stashedSession,
        ),
        savedStateConfiguration,
      )
    }
  }

  /**
   * Whether a pending deep link stashed at [stashedAtEpochMs] is recent enough (as of [nowEpochMs]) to
   * still be landed after a process-death restore. `null` (no timestamp — e.g. a pre-upgrade snapshot)
   * and a negative age (clock moved backwards) both count as not-fresh, so the link is dropped rather
   * than risk landing a stale one. Pure and clock-injected so it is unit-testable without Android.
   */
  internal fun isPendingDeepLinkStashTimeFresh(stashedAtEpochMs: Long?, nowEpochMs: Long): Boolean {
    if (stashedAtEpochMs == null) return false
    val ageMs = nowEpochMs - stashedAtEpochMs
    return ageMs in 0..MAX_PENDING_DEEP_LINK_AGE.inWholeMilliseconds
  }
}

/**
 * The full hoisted navigation state, serialized into the Activity's SavedStateRegistry so the
 * in-memory [BackstackController] singleton can be re-hydrated after process death. Mirrors the
 * holders the controller owns.
 *
 * [pendingDeepLinkStashedAtEpochMs] defaults to `null` so snapshots written before this field existed
 * still decode; a missing timestamp means an age can't be computed, so such a restored pending link is
 * treated as stale and dropped (see [NavigationStateBridge.restoreAndPersist]).
 */
@Serializable
private data class NavStateSnapshot(
  val entries: List<@Polymorphic HedvigNavKey>,
  val parkedRuns: Map<TopLevelTab, List<@Polymorphic HedvigNavKey>>,
  val pendingDeepLink: (@Polymorphic HedvigNavKey)?,
  val stashedSession: StashedSession?,
  val pendingDeepLinkStashedAtEpochMs: Long? = null,
)
