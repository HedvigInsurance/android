package com.hedvig.android.app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.StashedSession
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.LoneDeepLinkChrome

/**
 * The per-Activity source of truth for all navigation state and the one [Backstack] every caller in
 * that Activity talks to — the UI via this concrete type, Presenters via the injected [Backstack]
 * interface (bound in the per-Activity `ActivityRetainedGraph`).
 *
 * Why activity-retained instead of composition: the state used to live in `rememberSerializable`
 * inside `MainActivity.setContent`, so a config change recreated the Activity and deserialized it into
 * a brand-new instance — while Metro ViewModels survive a config change as the same instance via their
 * per-entry `ViewModelStore`. Handing a long-lived Presenter a reference to the composition-scoped
 * stack would therefore go stale on the next rotation. The controller is instead built by
 * [com.hedvig.android.app.navigation.NavRetainedViewModel] (a retained `ViewModel`), so it outlives
 * the composition and every screen ViewModel across a config change, yet — unlike the previous
 * app-singleton — dies with its Activity. Two `MainActivity` instances therefore get two independent
 * controllers instead of sharing one.
 *
 * Process-death persistence is bridged at the Activity seam: the retained instance is wiped when the
 * process dies, so `MainActivity` serializes the four holders into its `SavedStateRegistry` and
 * re-hydrates them on a cold start via [restoreFromSavedState]. A config change reuses the live
 * retained instance untouched (the restore is a no-op once populated).
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
   * Attached by `MainActivity` in `onCreate` (see `attachBackstackTaskHooks`), not at construction: the
   * controller is owned by this Activity's retained `ViewModel` and is 1:1 with the Activity, so there
   * is no shared, process-wide controller a backgrounded Activity could steal — the hook is wired once
   * at creation, with no resume re-attachment needed. Defaults to `true` so unit tests and any
   * pre-attach use stay fully in-process.
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
   * Drives the scene decorator (D11). The navigation suite (tab bar/rail) may render only while the
   * stack is inside our runs model: our own task AND rooted at [HomeKey] (or [LoginKey], which never
   * shows a suite anyway). A lone deep link — or any stack that grew from one before [navigateUp]
   * re-rooted it at Home — is *outside* that model. The runs helpers in `TopLevelRunLogic` assume
   * `HomeKey` at index 0 and silently no-op otherwise, so rendering the suite there strands the user
   * (a dead Home tap). Outside the model we suppress the suite and instead key off the *top* (rendered)
   * entry: a bare top-level root gets a decorator-supplied Up-bar (it has no back affordance of its
   * own), a deeper screen gets nothing (it draws its own Up). A lone Home hosted in a foreign task is
   * itself a top-level root, so it gets the Up-bar — letting the user escape back via Up (see
   * [navigateUp]).
   */
  val loneDeepLinkChrome: LoneDeepLinkChrome
    get() {
      val first = entries.firstOrNull()
      val insideRunsModel = isOwnTask() && (first is HomeKey || first is LoginKey)
      if (insideRunsModel) return LoneDeepLinkChrome.ShowSuite
      return if (entries.lastOrNull()?.topLevelTabOrNull() != null) {
        LoneDeepLinkChrome.ShowUpBar
      } else {
        LoneDeepLinkChrome.ShowNothing
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
   * Task-aware Up. For a lone deep link it materializes the parent ancestry — `[Home]` for a lone
   * tab root, `[Home, Insurances]` for a lone contract detail — so Up behaves exactly like Back would
   * have inside the app. When we were launched into a foreign task ([isOwnTask] is false) that parent
   * stack is handed to [escapeToOwnTask], which re-roots the app in its own task; otherwise it is
   * materialized in place. A lone leaf with no ancestry (e.g. Home) hosted in a foreign task has no
   * parent to rebuild, but must still escape so the runs model — and its nav bar — come alive; the
   * escape is seeded with the leaf itself. Everywhere else Up is a plain temporal pop, identical to
   * Back (a no-op at the root — it must not exit the app the way Back does).
   */
  override fun navigateUp(): Boolean {
    val top = entries.lastOrNull() ?: return false
    if (entries.size == 1) {
      val synthetic = syntheticStackFor(top)
      val parentStack = synthetic.dropLast(1)
      when {
        parentStack.isNotEmpty() -> {
          if (isOwnTask()) entries.replaceWith(parentStack) else escapeToOwnTask(parentStack)
          return true
        }

        !isOwnTask() -> {
          escapeToOwnTask(synthetic)
          return true
        }
      }
    }
    // No escape needed (own-task lone root, or a normal multi-entry stack): a pure pop, a no-op at the root.
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
   * Pops to [index] (see [Backstack.popUpToIndex]). A negative [index] means the caller asked to clear
   * the base too; super keeps the base rendered (never an empty stack) and we finish the host so the
   * app exits — same finish-at-root contract as [popBackstack].
   */
  override fun popUpToIndex(index: Int) {
    super.popUpToIndex(index)
    if (index < 0) finishApp()
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
