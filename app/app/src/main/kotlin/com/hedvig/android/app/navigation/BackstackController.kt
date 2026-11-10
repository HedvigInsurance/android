package com.hedvig.android.app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.hedvig.android.navigation.common.SuppressesAppStoreReviewRequest
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.contentKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.LoneDeepLinkChrome
import kotlin.time.Clock

/**
 * The per-Activity source of truth for all navigation state and the one [Backstack] every caller in
 * that Activity talks to — the UI via this concrete type, Presenters via the injected [Backstack]
 * interface (bound in the per-Activity `ActivityRetainedGraph`).
 *
 * Why activity-retained rather than composition-scoped: the controller is built by
 * [com.hedvig.android.app.navigation.NavRetainedViewModel] (a retained `ViewModel`), so it outlives
 * the composition and every screen ViewModel across a config change — a long-lived Presenter can hold
 * a reference to it without it going stale on rotation — yet it dies with its Activity. Two
 * `MainActivity` instances therefore get two independent controllers instead of sharing one.
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
  // Defaulted (and placed after the required states) so the many positional test constructions need no
  // change; the real construction in NavRetainedViewModel passes it explicitly by name.
  pendingDeepLinkStashedAtState: MutableState<Long?> = mutableStateOf(null),
  /**
   * Re-roots the app in its own task seeded with the given stack (see [navigateUp]). The Activity
   * owns the mechanics (relaunch with NEW_TASK|CLEAR_TASK + finish); the controller only supplies
   * the target stack. Attached by the Activity in `onCreate`; no-op by default.
   */
  var escapeToOwnTask: (List<HedvigNavKey>) -> Unit = {},
  /**
   * Finishes the host Activity, used by [popBackstack] when a Back/close lands on the root (nothing
   * left to pop) so the app exits instead of stranding the user on a dead screen. Attached by the
   * Activity in `onCreate`; no-op by default so unit tests and any pre-attach use never try to finish.
   */
  var finishApp: () -> Unit = {},
  private val clock: Clock = Clock.System,
) : Backstack {
  /**
   * A deep link resolved while logged out, held until [setLoggedIn] lands it. Persisted across rotation
   * and process death (e.g. mid-OTP) — see the class KDoc. Always set via [stashPendingDeepLink] so its
   * companion timestamp [pendingDeepLinkStashedAtEpochMs] stays in sync; cleared (with the timestamp)
   * when consumed by [setLoggedIn] or wiped by [reseed].
   */
  internal var pendingDeepLink: HedvigNavKey? by pendingDeepLinkState

  /**
   * Epoch milliseconds, read from [clock], when [pendingDeepLink] was last stashed, or `null`
   * when there is none. Persisted alongside [pendingDeepLink] so a process-death restore can discard a
   * link that is too old to still belong to the in-flight login (see `NavigationStateBridge`), which
   * stops a long-abandoned pending link from bleeding into an unrelated later login.
   */
  internal var pendingDeepLinkStashedAtEpochMs: Long? by pendingDeepLinkStashedAtState

  /** Stashes [key] as the [pendingDeepLink], stamping [pendingDeepLinkStashedAtEpochMs] with now. */
  private fun stashPendingDeepLink(key: HedvigNavKey) {
    pendingDeepLink = key
    pendingDeepLinkStashedAtEpochMs = clock.now().toEpochMilliseconds()
  }

  /** Clears [pendingDeepLink] and its companion timestamp together. */
  private fun clearPendingDeepLink() {
    pendingDeepLink = null
    pendingDeepLinkStashedAtEpochMs = null
  }

  /**
   * The previous logged-in session, held between logout and the next login. Excluded from
   * [allLiveContentKeys] on purpose — see [StashedSession]. Persisted across process death.
   */
  internal var stashedSession: StashedSession? by stashedSessionState

  val isLoggedIn: Boolean
    get() = entries.firstOrNull() !is LoginKey

  val currentTopLevel: TopLevelTab
    get() = nearestTopLevelTab(entries) ?: TopLevelTab.Home

  /** The destination on top of the rendered stack. */
  val currentDestination: HedvigNavKey?
    get() = entries.lastOrNull()

  /**
   * Whether the member is anywhere inside a flow that must not be interrupted by the Play Store
   * review prompt. Checks the whole stack, not just the top, because such a flow hosts the screens
   * that earn the prompt on top of itself.
   */
  val suppressesAppStoreReviewRequest: Boolean
    get() = entries.any { it is SuppressesAppStoreReviewRequest }

  /**
   * Every key whose decorator state must survive: the rendered stack plus all parked runs, mapped
   * to their [contentKey]. The retained decorators consult this set in `onPop` so a
   * key that merely moved into [parkedRuns] keeps its saved state and ViewModel.
   */
  val allLiveContentKeys: Set<Any>
    get() = buildSet {
      entries.forEach { add(it.contentKey()) }
      parkedRuns.values.forEach { run -> run.forEach { add(it.contentKey()) } }
    }

  /**
   * [contentKey] → the top-level tab that owns it, used by the [HedvigNavDisplay]
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
      tab?.let { owningTabByContentKey[key.contentKey()] = it }
    }
    parkedRuns.forEach { (parkedTab, run) ->
      run.forEach { owningTabByContentKey[it.contentKey()] = parkedTab }
    }
    return owningTabByContentKey[contentKey.toString()]
  }

  /**
   * Whether the stack is inside the runs model: rooted at [HomeKey] (or [LoginKey]). The only stack
   * that is not is a deep link that landed alone because another app's task hosts us (see
   * [navigateToExternalDeepLink]), plus whatever was pushed on top of it. The stack shape is the
   * record of that, so no separate task state is kept.
   */
  private val isRootedInRunsModel: Boolean
    get() = entries.firstOrNull().let { it is HomeKey || it is LoginKey }

  /**
   * Drives the scene decorator. The navigation suite (tab bar/rail) may render only inside the runs
   * model: the runs helpers in `TopLevelRunLogic` assume `HomeKey` at index 0 and silently no-op
   * otherwise, so rendering the suite on a lone deep link strands the user (a dead Home tap). Outside
   * the model the decision keys off the top entry: a bare top-level root gets a decorator-supplied
   * Up-bar (it has no back affordance of its own), a deeper screen gets nothing (it draws its own Up).
   */
  val loneDeepLinkChrome: LoneDeepLinkChrome
    get() = when {
      isRootedInRunsModel -> LoneDeepLinkChrome.ShowSuite
      entries.lastOrNull()?.topLevelTabOrNull() != null -> LoneDeepLinkChrome.ShowUpBar
      else -> LoneDeepLinkChrome.ShowNothing
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
   * Routes an in-app link tap (Compose `LocalUriHandler`). The member is navigating *within* a live
   * session, so it joins the live stack: logged out, stash it (landed by [setLoggedIn]); logged in,
   * dedup and append. External / notification deep links use [navigateToExternalDeepLink] instead,
   * which re-roots the stack.
   */
  fun navigateToInAppLink(key: HedvigNavKey) {
    if (!isLoggedIn) {
      stashPendingDeepLink(key)
      return
    }
    Snapshot.withMutableSnapshot {
      entries.remove(key)
      entries.add(key)
    }
  }

  /**
   * Routes an external or notification deep link: an entry into the app from outside, not navigation
   * within a live session, so it replaces whatever was there. Logged out, stash it as
   * [pendingDeepLink] for [setLoggedIn]. Logged in, what it lands on depends on which task hosts us:
   *  - our own task: the synthetic ancestry from [syntheticStackFor], so Back and Up both walk the
   *    path the member would have taken by hand before leaving the app;
   *  - another app's task ([hostedInForeignTask]): the key alone, so Back returns straight to that
   *    app, and Up re-roots us in our own task (see [navigateUp]).
   */
  fun navigateToExternalDeepLink(key: HedvigNavKey, hostedInForeignTask: Boolean) {
    if (!isLoggedIn) {
      stashPendingDeepLink(key)
      return
    }
    reseed(if (hostedInForeignTask) listOf(key) else syntheticStackFor(key))
  }

  /**
   * Up is a plain temporal pop, identical to Back except in two places. At the root it is a no-op: Up
   * must never exit the app the way Back does. And on a deep link standing alone in another app's task
   * it hands the key's parent ancestry to [escapeToOwnTask], so the member continues in our own task
   * instead of falling back into the app that linked here.
   */
  override fun navigateUp(): Boolean {
    val loneDeepLink = entries.singleOrNull()?.takeIf { !isRootedInRunsModel }
    if (loneDeepLink != null) {
      escapeToOwnTask(syntheticStackFor(loneDeepLink).dropLast(1))
      return true
    }
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
      clearPendingDeepLink()
      val stash = stashedSession?.takeIf { memberId != null && it.memberId == memberId }
      stashedSession = null
      parkedRuns.clear()
      when {
        pending != null -> {
          entries.replaceWith(syntheticStackFor(pending))
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
      clearPendingDeepLink()
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
    pendingDeepLinkStashedAtEpochMs: Long?,
    stashedSession: StashedSession?,
  ) {
    if (this.entries.isNotEmpty()) return
    Snapshot.withMutableSnapshot {
      this.entries.addAll(entries)
      this.parkedRuns.putAll(parkedRuns)
      this.pendingDeepLink = pendingDeepLink
      this.pendingDeepLinkStashedAtEpochMs = pendingDeepLinkStashedAtEpochMs
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
