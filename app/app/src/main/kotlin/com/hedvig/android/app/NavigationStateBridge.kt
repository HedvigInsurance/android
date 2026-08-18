package com.hedvig.android.app

import android.app.Activity
import android.content.Intent
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
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * The single seam between an Activity lifecycle and the app-scoped [BackstackController]. Owns every
 * way the navigation state crosses that seam, so the decision about what the stack should be at
 * launch is made in one place rather than spread across `MainActivity`:
 *
 *  - the Intent escape handoff that carries a backstack across an Activity relaunch ([escapeToOwnTask]
 *    writes it, [restoreAndPersist] reads it),
 *  - the process-death snapshot serialized into the Activity's `SavedStateRegistry` ([NavStateSnapshot]),
 *  - the launch-time seeding precedence and the save-provider registration ([restoreAndPersist]).
 */
internal object NavigationStateBridge {
  private const val EXTRA_RESTORE_STACK = "com.hedvig.android.app.RESTORE_STACK"
  private const val NAV_STATE_REGISTRY_KEY = "com.hedvig.android.app.NAV_STATE"

  /**
   * How long a [BackstackController.pendingDeepLink] may survive a process death and still be landed at
   * the next login. Covers a real "killed mid-login" window (switching to BankID, OTP, etc., is seconds
   * to a few minutes) while discarding a link stashed long ago, which would otherwise bleed into an
   * unrelated later login and strand the member on a lone deep-link stack. See [restoreAndPersist].
   */
  private val MAX_PENDING_DEEP_LINK_AGE = 30.minutes

  private val handoffSerializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))

  /**
   * Seeds / restores the hoisted navigation state, then registers the provider that persists it.
   * The seeding precedence, read top to bottom:
   *  1. An explicit deep-link / escape re-root (carried on the launch [intent]) replaces everything.
   *  2. Otherwise, on a cold start after process death, re-hydrate the full state from the Activity's
   *     `SavedStateRegistry`. On a config change the live singleton is already populated, so
   *     [BackstackController.restoreFromSavedState] is a no-op and the live state wins.
   *  3. Finally guarantee at least a Login root.
   *
   * [isColdStart] is `savedInstanceState == null`: a handoff only applies to a genuinely fresh launch.
   */
  fun restoreAndPersist(
    backstackController: BackstackController,
    savedStateRegistry: SavedStateRegistry,
    intent: Intent,
    isColdStart: Boolean,
    serializersModules: Set<SerializersModule>,
  ) {
    val savedStateConfiguration = SavedStateConfiguration {
      this.serializersModule = serializersModules.merge()
    }

    val handoff = if (isColdStart) {
      readEscapeToOwnTaskHandoff(intent, serializersModules)
    } else {
      null
    }
    if (!handoff.isNullOrEmpty()) {
      backstackController.reseed(handoff)
    } else {
      val snapshot = savedStateRegistry.consumeRestoredStateForKey(NAV_STATE_REGISTRY_KEY)
        ?.let { decodeFromSavedState(NavStateSnapshot.serializer(), it, savedStateConfiguration) }
      if (snapshot != null) {
        // Discard a pending deep link that is too old to still belong to an in-flight login, so it
        // can't bleed into an unrelated later login as a lone deep-link stack.
        val now = System.currentTimeMillis()
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
    }

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
   * Finishes this foreign-hosted instance and relaunches MainActivity in its own task, seeded with
   * [parentStack]. The fresh instance reads the ancestry back in [restoreAndPersist] via the same
   * extra key and codec, so the write/read contract can't drift.
   */
  fun escapeToOwnTask(
    activity: Activity,
    parentStack: List<HedvigNavKey>,
    serializersModules: Set<SerializersModule>,
  ) {
    val relaunch = Intent(activity, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
      putExtra(EXTRA_RESTORE_STACK, json(serializersModules).encodeToString(handoffSerializer, parentStack))
    }
    activity.finish()
    activity.startActivity(relaunch)
  }

  /** The ancestry seeded by a prior [escapeToOwnTask], or null when [intent] carries no handoff. */
  private fun readEscapeToOwnTaskHandoff(
    intent: Intent,
    serializersModules: Set<SerializersModule>,
  ): List<HedvigNavKey>? {
    val encoded = intent.getStringExtra(EXTRA_RESTORE_STACK) ?: return null
    return json(serializersModules).decodeFromString(handoffSerializer, encoded)
  }

  private fun json(serializersModules: Set<SerializersModule>): Json = Json {
    serializersModule = serializersModules.merge()
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
