package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateMapSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer

@Stable
internal class BackstackController(
  override val entries: SnapshotStateList<HedvigNavKey>,
  internal val parkedRuns: SnapshotStateMap<TopLevelGraph, List<HedvigNavKey>>,
) : Backstack {
  val isLoggedIn: Boolean
    get() = entries.firstOrNull()?.topLevelGraphOrNull() != null

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
   * Routes a resolved deep-link key onto the stack without creating a value-equal duplicate. Tab
   * roots go through [selectTopLevel] (which restores any parked run); any other key already present
   * is moved to the top rather than re-appended; a genuinely new key is appended.
   */
  fun navigateToDeepLink(key: HedvigNavKey) {
    val topLevelGraph = key.topLevelGraphOrNull()
    if (topLevelGraph != null) {
      selectTopLevel(topLevelGraph)
      return
    }
    Snapshot.withMutableSnapshot {
      entries.remove(key)
      entries.add(key)
    }
  }

  /** Move into the tabbed shell, Home pinned at the base; forget any parked runs. */
  fun setLoggedIn() {
    Snapshot.withMutableSnapshot {
      parkedRuns.clear()
      entries.clear()
      entries.add(HomeKey)
    }
  }

  /** Drop back to the login root; forget any parked runs. */
  fun setLoggedOut() {
    Snapshot.withMutableSnapshot {
      parkedRuns.clear()
      entries.clear()
      entries.add(LoginKey)
    }
  }
}

private fun SnapshotStateList<HedvigNavKey>.replaceWith(target: List<HedvigNavKey>) {
  if (this == target) return
  Snapshot.withMutableSnapshot {
    clear()
    addAll(target)
  }
}

@Composable
internal fun rememberHedvigBackstackController(savedStateConfiguration: SavedStateConfiguration): BackstackController {
  val backstack = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
  ) {
    mutableStateListOf<HedvigNavKey>(LoginKey)
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
  return remember(backstack, parkedRuns) { BackstackController(backstack, parkedRuns) }
}
