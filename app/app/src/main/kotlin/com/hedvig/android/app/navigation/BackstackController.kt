package com.hedvig.android.app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.StashedSession
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.LoneDeepLinkChrome
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * The single, app-scoped source of truth for all navigation state and the one [Backstack] every
 * caller talks to — the UI via this concrete type, Presenters via the injected [Backstack] interface
 * (bound in [BackstackControllerProviders]).
 *
 * Why app-scoped instead of composition: the state used to live in `rememberSerializable` inside
 * `MainActivity.setContent`, so a config change recreated the Activity and deserialized it into a
 * brand-new instance — while Metro ViewModels survive a config change as the same instance via their
 * per-entry `ViewModelStore`. Handing a long-lived Presenter a reference to the composition-scoped
 * stack would therefore go stale on the next rotation. Owning the snapshot state in this app-scoped
 * singleton (see [BackstackControllerProviders]) makes the controller outlive every ViewModel, so a
 * Presenter can mutate the live, rendered stack through [Backstack].
 *
 * Process-death persistence is bridged at the Activity seam: an in-memory singleton is wiped when the
 * process dies, so `MainActivity` serializes the four holders into its `SavedStateRegistry` and
 * re-hydrates them on a cold start via [restoreFromSavedState]. A config change reuses the live
 * singleton untouched (the restore is a no-op once populated).
 */
@Stable
internal class BackstackController(
  override val entries: SnapshotStateList<HedvigNavKey>,
  internal val parkedRuns: SnapshotStateMap<TopLevelTab, List<HedvigNavKey>>,
  pendingDeepLinkState: MutableState<HedvigNavKey?>,
  stashedSessionState: MutableState<StashedSession?>,
  /**
   * Whether this activity is the root of its own task. `false` means we were launched into the
   * caller's task by an external deep link, so an Up press must escape into our own task rather than
   * rebuilding the ancestry in place (which would leave our screens hosted under the foreign app).
   *
   * Attached by `MainActivity` on resume (not at construction): this controller is an app-singleton
   * shared by every Activity in the process, so re-pointing the hook on each resume keeps it tracking
   * the foreground Activity instead of letting a later-created but backgrounded Activity win. Defaults
   * to `true` so unit tests and any pre-attach use stay fully in-process.
   */
  var isOwnTask: () -> Boolean = { true },
  /**
   * Re-roots the app in its own task seeded with the given stack (see [navigateUp]). The Activity
   * owns the mechanics (relaunch with NEW_TASK|CLEAR_TASK + finish); the controller only supplies
   * the target stack. Attached/replaced by the Activity like [isOwnTask]; no-op by default.
   */
  var escapeToOwnTask: (List<HedvigNavKey>) -> Unit = {},
  /**
   * Finishes the host Activity, used by [popBackstack] when a Back/close lands on the root (nothing
   * left to pop) so the app exits instead of stranding the user on a dead screen. Attached/replaced
   * by the Activity like [isOwnTask] and [escapeToOwnTask]; no-op by default so unit tests and any
   * pre-attach use never try to finish.
   */
  var finishApp: () -> Unit = {},
) : Backstack {
  /**
   * A deep link resolved while logged out, held until [setLoggedIn] consumes it (so it can land
   * alone). Persisted across rotation / process death (e.g. mid-OTP) — see the class KDoc.
   */
  internal var pendingDeepLink: HedvigNavKey? by pendingDeepLinkState

  /**
   * The previous logged-in session, held between logout and the next login. Excluded from
   * [allLiveContentKeys] on purpose — see [StashedSession]. Persisted across process death.
   */
  internal var stashedSession: StashedSession? by stashedSessionState

  val isLoggedIn: Boolean
    get() = entries.firstOrNull() !is LoginKey

  val currentTopLevel: TopLevelTab
    get() = nearestTopLevelTab(entries) ?: TopLevelTab.Home

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
   * `contentKey` (`toString()`) → the top-level tab that owns it, used by the [HedvigNavDisplay]
   * transition classifier to fade between tabs and slide within one. A screen's owner is *positional*
   * (which run it sits in), so it can't be read off a single key in isolation; this resolves it from
   * the full rendered stack plus all [parkedRuns].
   *
   * The map accumulates and is never cleared: a key just popped by [popBackstack] (gone from both
   * [entries] and [parkedRuns]) keeps its last-known owner so the *outgoing* scene of that pop can
   * still be classified (e.g. system-back from a side-tab root to Home stays a fade). A given key
   * type only ever lives in one tab's run, so a retained owner can't go stale.
   */
  private val owningTabByContentKey = mutableMapOf<String, TopLevelTab>()

  fun owningTopLevelTabForContentKey(contentKey: Any?): TopLevelTab? {
    if (contentKey == null) return null
    var tab: TopLevelTab? = null
    entries.forEach { key ->
      tab = key.topLevelTabOrNull() ?: tab
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
        first?.topLevelTabOrNull() != null -> LoneDeepLinkChrome.ShowUpBar
        else -> LoneDeepLinkChrome.ShowNothing
      }
    }

  /**
   * Rail/bar tap. Re-tapping the current tab pops its run to the root. Switching tabs stashes the
   * leaving side-tab's run into [parkedRuns] (Home is never parked — it stays in the rendered stack)
   * and restores the target tab's parked run, or starts a fresh one.
   */
  fun selectTopLevel(topLevelTab: TopLevelTab) {
    Snapshot.withMutableSnapshot {
      if (topLevelTab == currentTopLevel) {
        entries.replaceWith(popTopRunToStart(entries))
        return@withMutableSnapshot
      }
      val leavingSideTab = nearestTopLevelTab(entries)?.takeIf { it != TopLevelTab.Home }
      val homeRun = collapseToHome(entries)
      if (leavingSideTab != null) {
        parkedRuns[leavingSideTab] = activeSideRun(entries)
      }
      val restored = if (topLevelTab == TopLevelTab.Home) {
        homeRun
      } else {
        homeRun + (parkedRuns.remove(topLevelTab) ?: listOf(topLevelTab.startDestination))
      }
      entries.replaceWith(restored)
    }
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
    // Fall through to a *pure* pop (not our finishing override): an Up press at the root is a no-op,
    // it must not exit the app the way Back does.
    return super.popBackstack()
  }

  /**
   * Back/close pop. Pops the top entry; if there is nothing to pop (we are at the root) the Activity
   * is finished so the app exits rather than leaving the user on a screen where Back does nothing.
   */
  override fun popBackstack(): Boolean {
    val popped = super.popBackstack()
    if (!popped) finishApp()
    return popped
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

  /**
   * Seeds the rendered stack the first time the process starts. A no-op once populated, so a
   * recreated Activity (config change) reuses the live stack instead of re-seeding it.
   */
  fun seedIfEmpty(initial: List<HedvigNavKey>) {
    if (entries.isEmpty()) {
      Snapshot.withMutableSnapshot { entries.addAll(initial) }
    }
  }

  /**
   * Replaces the whole navigation state with a fresh root — used by the Activity for an explicit
   * re-root (deep-link / escape into our own task). Clears the auxiliary state too: an escape
   * relaunches with `CLEAR_TASK` while the process (and this singleton) may survive, so parked runs /
   * pending deep link / stash from the old task must not bleed into the new root.
   */
  fun reseed(stack: List<HedvigNavKey>) {
    Snapshot.withMutableSnapshot {
      entries.replaceWith(stack)
      parkedRuns.clear()
      pendingDeepLink = null
      stashedSession = null
    }
  }

  /**
   * Re-hydrates the full navigation state after process death. Only applies when the live state is
   * empty (a genuine cold start); on a config change the singleton is already populated and this is a
   * no-op, so the live state always wins over the serialized snapshot.
   */
  fun restoreFromSavedState(
    entries: List<HedvigNavKey>,
    parkedRuns: Map<TopLevelTab, List<HedvigNavKey>>,
    pendingDeepLink: HedvigNavKey?,
    stashedSession: StashedSession?,
  ) {
    if (this.entries.isNotEmpty()) return
    Snapshot.withMutableSnapshot {
      this.entries.addAll(entries)
      this.parkedRuns.putAll(parkedRuns)
      this.pendingDeepLink = pendingDeepLink
      this.stashedSession = stashedSession
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

/**
 * Wires the app-scoped navigation state. The four snapshot holders are created once and owned by the
 * singleton [BackstackController] — their lifetime is the application graph, so they survive a config
 * change while remaining the live objects the UI renders. The controller is exposed to feature
 * Presenters as a plain [Backstack].
 */
@ContributesTo(AppScope::class)
internal interface BackstackControllerProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideBackstackController(): BackstackController = BackstackController(
    entries = mutableStateListOf(),
    parkedRuns = mutableStateMapOf(),
    pendingDeepLinkState = mutableStateOf(null),
    stashedSessionState = mutableStateOf(null),
  )

  @Provides
  fun bindBackstack(controller: BackstackController): Backstack = controller
}
