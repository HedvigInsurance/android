package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateMapSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.LoneDeepLinkChrome
import com.hedvig.android.navigation.compose.popBackstack
import com.hedvig.android.navigation.common.TopLevelGraph
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable

/**
 * The logged-in session captured at logout: the rendered [entries] plus all [parkedRuns], tagged
 * with the [memberId] (JWT `sub`) it belonged to. Held by [BackstackController.stashedSession],
 * which is intentionally absent from [BackstackController.allLiveContentKeys] so the retained
 * decorators dispose every key's per-entry state while the session sits here. Restored on a
 * same-member login by [BackstackController.setLoggedIn]; persisted across process death.
 */
@Serializable
internal data class StashedSession(
  val memberId: String,
  val entries: List<@Polymorphic HedvigNavKey>,
  val parkedRuns: Map<TopLevelGraph, List<@Polymorphic HedvigNavKey>>,
)

@Stable
internal class BackstackController(
  override val entries: SnapshotStateList<HedvigNavKey>,
  internal val parkedRuns: SnapshotStateMap<TopLevelGraph, List<HedvigNavKey>>,
  pendingDeepLinkState: MutableState<HedvigNavKey?>,
  stashedSessionState: MutableState<StashedSession?>,
  /**
   * Whether this activity is the root of its own task. `false` means we were launched into the
   * caller's task by an external deep link, so an Up press must escape into our own task rather than
   * rebuilding the ancestry in place (which would leave our screens hosted under the foreign app).
   * Defaults to `true` so unit tests and any non-Activity construction stay fully in-process.
   */
  private val isOwnTask: () -> Boolean = { true },
  /**
   * Re-roots the app in its own task seeded with the given stack (see [navigateUp]). The Activity
   * owns the mechanics (relaunch with NEW_TASK|CLEAR_TASK + finish); the controller only supplies
   * the target stack. No-op by default so the in-process path is the testable default.
   */
  private val escapeToOwnTask: (List<HedvigNavKey>) -> Unit = {},
) : Backstack {
  /**
   * A deep link resolved while logged out, held until [setLoggedIn] consumes it (so it can land
   * alone). Persisted across rotation / process death (e.g. mid-OTP) via [rememberHedvigBackstackController].
   */
  internal var pendingDeepLink: HedvigNavKey? by pendingDeepLinkState

  /**
   * The previous logged-in session, held between logout and the next login. Excluded from
   * [allLiveContentKeys] on purpose — see [StashedSession]. Persisted across process death via
   * [rememberHedvigBackstackController].
   */
  internal var stashedSession: StashedSession? by stashedSessionState

  val isLoggedIn: Boolean
    get() = entries.firstOrNull() !is LoginKey

  val currentTopLevel: TopLevelGraph
    get() = nearestTopLevelGraph(entries) ?: TopLevelGraph.Home

  /** The destination on top of the rendered stack — replaces Nav2's `navController.currentDestination`. */
  val currentDestination: HedvigNavKey?
    get() = entries.lastOrNull()

  /**
   * Every key whose decorator state must survive: the rendered stack plus all parked runs, mapped
   * to their `contentKey` (`toString()`). The retained decorators consult this set in `onPop` so a
   * key that merely moved into [parkedRuns] keeps its saved state and ViewModel.
   */
  val allLiveContentKeys: Set<Any>
    get() = buildSet {
      entries.forEach { add(it.toString()) }
      parkedRuns.values.forEach { run -> run.forEach { add(it.toString()) } }
    }

  /**
   * `contentKey` (`toString()`) → the top-level graph that owns it, used by the [HedvigNavDisplay]
   * transition classifier to fade between tabs and slide within one. A screen's owner is *positional*
   * (which run it sits in), so it can't be read off a single key in isolation; this resolves it from
   * the full rendered stack plus all [parkedRuns].
   *
   * The map accumulates and is never cleared: a key just popped by [handleBack] (gone from both
   * [entries] and [parkedRuns]) keeps its last-known owner so the *outgoing* scene of that pop can
   * still be classified (e.g. system-back from a side-tab root to Home stays a fade). A given key
   * type only ever lives in one tab's run, so a retained owner can't go stale.
   */
  private val owningTabByContentKey = mutableMapOf<String, TopLevelGraph>()

  fun owningTopLevelGraphForContentKey(contentKey: Any?): TopLevelGraph? {
    if (contentKey == null) return null
    var tab: TopLevelGraph? = null
    entries.forEach { key ->
      tab = key.topLevelGraphOrNull() ?: tab
      tab?.let { owningTabByContentKey[key.toString()] = it }
    }
    parkedRuns.forEach { (parkedTab, run) ->
      run.forEach { owningTabByContentKey[it.toString()] = parkedTab }
    }
    return owningTabByContentKey[contentKey.toString()]
  }

  /**
   * Drives the scene decorator (D11). A lone non-Home/non-Login key suppresses the rail: a tab root
   * gets a decorator-supplied Up-bar, a deep bar-keeper (which renders its own Up) gets nothing.
   */
  val loneDeepLinkChrome: LoneDeepLinkChrome
    get() {
      val first = entries.firstOrNull()
      val isAlone = entries.size == 1 && first !is HomeKey && first !is LoginKey
      return when {
        !isAlone -> LoneDeepLinkChrome.ShowSuite
        first?.topLevelGraphOrNull() != null -> LoneDeepLinkChrome.ShowUpBar
        else -> LoneDeepLinkChrome.ShowNothing
      }
    }

  /**
   * Rail/bar tap. Re-tapping the current tab pops its run to the root. Switching tabs stashes the
   * leaving side-tab's run into [parkedRuns] (Home is never parked — it stays in the rendered stack)
   * and restores the target tab's parked run, or starts a fresh one.
   */
  fun selectTopLevel(topLevelGraph: TopLevelGraph) {
    Snapshot.withMutableSnapshot {
      if (topLevelGraph == currentTopLevel) {
        entries.replaceWith(popTopRunToStart(entries))
        return@withMutableSnapshot
      }
      val leavingSideTab = nearestTopLevelGraph(entries)?.takeIf { it != TopLevelGraph.Home }
      val homeRun = collapseToHome(entries)
      if (leavingSideTab != null) {
        parkedRuns[leavingSideTab] = activeSideRun(entries)
      }
      val restored = if (topLevelGraph == TopLevelGraph.Home) {
        homeRun
      } else {
        homeRun + (parkedRuns.remove(topLevelGraph) ?: listOf(topLevelGraph.startDestination))
      }
      entries.replaceWith(restored)
    }
  }

  /**
   * System-back handler. Returns false when the app should finish. A plain pop: the rendered stack
   * is always `homeRun + at most one side run`, so popping walks up the active run, returns a side
   * root to Home, and exits from the Home root.
   *
   * It deliberately does **not** park the run it drains. Draining a side tab with system back is the
   * Nav2 "drop it completely" path: each popped key leaves the rendered stack and is absent from
   * [parkedRuns], so [allLiveContentKeys] stops covering it and the decorators dispose its saved
   * state. Only [selectTopLevel] parks a run. (Parked runs for *other* tabs are untouched here.)
   */
  fun handleBack(): Boolean {
    if (entries.size <= 1) return false
    Snapshot.withMutableSnapshot {
      entries.removeAt(entries.lastIndex)
    }
    return true
  }

  /**
   * Routes a resolved deep-link key. Logged out: stash it (consumed by [setLoggedIn] to land alone).
   * Logged in: dedup and append onto the live stack (join the current task — Nav2 parity).
   */
  fun navigateToDeepLink(key: HedvigNavKey) {
    if (!isLoggedIn) {
      pendingDeepLink = key
      return
    }
    Snapshot.withMutableSnapshot {
      entries.remove(key)
      entries.add(key)
    }
  }

  /**
   * Task-aware Up. For a lone deep link (size 1 with a non-trivial synthetic stack) it materializes
   * the parent ancestry — `[Home]` for a lone tab root, `[Home, Insurances]` for a lone contract
   * detail — so Up behaves exactly like Back would have inside the app. When we were launched into a
   * foreign task ([isOwnTask] is false) that parent stack is handed to [escapeToOwnTask], which
   * re-roots the app in its own task; otherwise it is materialized in place. Everywhere else Up is a
   * plain temporal pop, identical to Back.
   */
  override fun navigateUp(): Boolean {
    val top = entries.lastOrNull() ?: return false
    val synthetic = syntheticStackFor(top)
    if (entries.size == 1 && synthetic.size > 1) {
      val parentStack = synthetic.dropLast(1)
      if (isOwnTask()) {
        entries.replaceWith(parentStack)
      } else {
        escapeToOwnTask(parentStack)
      }
      return true
    }
    return popBackstack()
  }

  /**
   * Enter the logged-in app. Precedence: a pending deep link lands alone (re-enabling the runs model
   * on the next Up); otherwise a stash tagged with this same [memberId] is restored (history comes
   * back, per-entry state having been disposed while stashed); otherwise a fresh Home. Any stash is
   * always consumed/dropped so it can never bleed into a later session.
   */
  fun setLoggedIn(memberId: String?) {
    Snapshot.withMutableSnapshot {
      val pending = pendingDeepLink
      pendingDeepLink = null
      val stash = stashedSession?.takeIf { memberId != null && it.memberId == memberId }
      stashedSession = null
      parkedRuns.clear()
      when {
        pending != null -> {
          entries.replaceWith(listOf(pending))
        }

        stash != null -> {
          parkedRuns.putAll(stash.parkedRuns)
          entries.replaceWith(stash.entries)
        }

        else -> {
          entries.replaceWith(listOf(HomeKey))
        }
      }
    }
  }

  /**
   * Drop to the login root. Stashes the live session (tagged with [memberId]) so a same-member
   * re-login can restore the history; the stash is excluded from [allLiveContentKeys], so the
   * decorators dispose every key's per-entry state while it waits. A null [memberId] (demo mode /
   * unknown identity) stashes nothing — that session can never be safely restored. Logging out while
   * the top destination is a [DeliberateLogoutOrigin] (Profile) is treated as an intentional sign-out,
   * so nothing is stashed even with a known [memberId] — restoring the member onto that screen would
   * be wrong.
   */
  fun setLoggedOut(memberId: String?) {
    Snapshot.withMutableSnapshot {
      val isDeliberateLogout = entries.lastOrNull() is DeliberateLogoutOrigin
      stashedSession = if (memberId != null && !isDeliberateLogout) {
        StashedSession(memberId, entries.toList(), parkedRuns.toMap())
      } else {
        null
      }
      parkedRuns.clear()
      entries.replaceWith(listOf(LoginKey))
    }
  }
}

private fun SnapshotStateList<HedvigNavKey>.replaceWith(target: List<HedvigNavKey>) {
  if (size == target.size && indices.all { this[it] == target[it] }) return
  Snapshot.withMutableSnapshot {
    clear()
    addAll(target)
  }
}

@Composable
internal fun rememberHedvigBackstackController(
  savedStateConfiguration: SavedStateConfiguration,
  initialBackstack: List<HedvigNavKey>? = null,
  isOwnTask: () -> Boolean = { true },
  escapeToOwnTask: (List<HedvigNavKey>) -> Unit = {},
): BackstackController {
  val backstack = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
  ) {
    // A fresh process relaunched by an Up-escape seeds the restored ancestry instead of the login
    // root; tokens persist across the relaunch so the member is still logged in.
    if (initialBackstack.isNullOrEmpty()) {
      mutableStateListOf<HedvigNavKey>(LoginKey)
    } else {
      mutableStateListOf<HedvigNavKey>().apply { addAll(initialBackstack) }
    }
  }
  val parkedRuns = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateMapSerializer(
      TopLevelGraph.serializer(),
      ListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
    ),
  ) {
    mutableStateMapOf<TopLevelGraph, List<HedvigNavKey>>()
  }
  val pendingDeepLink = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = MutableStateSerializer(PolymorphicSerializer(HedvigNavKey::class).nullable),
  ) {
    mutableStateOf<HedvigNavKey?>(null)
  }
  val stashedSession = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = MutableStateSerializer(StashedSession.serializer().nullable),
  ) {
    mutableStateOf<StashedSession?>(null)
  }
  return remember(backstack, parkedRuns, pendingDeepLink, stashedSession) {
    BackstackController(
      entries = backstack,
      parkedRuns = parkedRuns,
      pendingDeepLinkState = pendingDeepLink,
      stashedSessionState = stashedSession,
      isOwnTask = isOwnTask,
      escapeToOwnTask = escapeToOwnTask,
    )
  }
}
