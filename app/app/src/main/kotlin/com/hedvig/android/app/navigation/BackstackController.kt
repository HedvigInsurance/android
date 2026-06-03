package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.PolymorphicSerializer

/**
 * Owns the app's single Nav3 back stack for [androidx.navigation3.ui.NavDisplay].
 *
 * One flat [SnapshotStateList] is the sole source of truth. Logged out it is `[LoginKey, …]`. Logged
 * in, [HomeKey] is pinned at index 0 and contiguous per-tab "runs" sit above it. Feature graphs
 * mutate [entries] directly; the NavDisplay renders the same instance. [isLoggedIn] and
 * [currentTopLevel] are derived, so process-death restore reconstructs them for free.
 */
@Stable
internal class BackstackController(
  override val entries: SnapshotStateList<HedvigNavKey>,
) : Backstack {
  val isLoggedIn: Boolean
    get() = entries.firstOrNull()?.topLevelGraphOrNull() != null

  val currentTopLevel: TopLevelGraph
    get() = nearestTopLevelGraph(entries) ?: TopLevelGraph.Home

  /** The destination on top of the rendered stack — replaces Nav2's `navController.currentDestination`. */
  val currentDestination: HedvigNavKey?
    get() = entries.lastOrNull()

  /**
   * Rail/bar tap. Re-tapping the current tab pops its run to the root; selecting Home from a side tab
   * returns to Home (discarding parked runs); selecting a different side tab moves its run to the top
   * (preserving Home at the base and the other runs).
   */
  fun selectTopLevel(topLevelGraph: TopLevelGraph) {
    Snapshot.withMutableSnapshot {
      val target = when (topLevelGraph) {
        currentTopLevel -> popTopRunToStart(entries)
        TopLevelGraph.Home -> collapseToHome(entries)
        else -> moveRunToTop(entries, topLevelGraph)
      }
      entries.replaceWith(target)
    }
  }

  /**
   * System-back handler. Returns false when the app should finish. Drains the active run, returns a
   * non-Home tab root straight to Home (no wandering into parked tabs), and exits from the root.
   */
  fun handleBack(): Boolean {
    if (entries.size <= 1) return false
    val topTab = entries.last().topLevelGraphOrNull()
    Snapshot.withMutableSnapshot {
      if (topTab != null && topTab != TopLevelGraph.Home) {
        entries.replaceWith(collapseToHome(entries))
      } else {
        entries.removeAt(entries.lastIndex)
      }
    }
    return true
  }

  /**
   * Routes a resolved deep-link key onto the stack without ever creating a value-equal duplicate.
   * Nav3 renders each entry under `key.toString()`, so two equal keys crash. Tab roots go through
   * [selectTopLevel] (a deduped tab switch); any other key already present is moved to the top
   * rather than re-appended; a genuinely new key is appended.
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

  /** Move into the tabbed shell, Home pinned at the base. */
  fun setLoggedIn() {
    Snapshot.withMutableSnapshot {
      entries.clear()
      entries.add(HomeKey)
    }
  }

  /** Drop back to the login root. */
  fun setLoggedOut() {
    Snapshot.withMutableSnapshot {
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
internal fun rememberHedvigBackstackController(
  savedStateConfiguration: SavedStateConfiguration,
): BackstackController {
  val backstack = rememberSerializable(
    configuration = savedStateConfiguration,
    serializer = SnapshotStateListSerializer(PolymorphicSerializer(HedvigNavKey::class)),
  ) {
    mutableStateListOf<HedvigNavKey>(LoginKey)
  }
  return remember(backstack) { BackstackController(backstack) }
}
