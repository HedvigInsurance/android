package com.hedvig.android.app.navigation

import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.navigation.common.DeepLinkAncestry
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot

/** Reverse of [startDestination]: the tab this key is the root of, or null if it is not a tab root. */
internal fun HedvigNavKey.topLevelTabOrNull(): TopLevelTab? = (this as? TopLevelTabRoot)?.topLevelTab

/** The tab owning the top entry: nearest tab key at or below the top. Null if the stack has no tab key. */
internal fun nearestTopLevelTab(stack: List<HedvigNavKey>): TopLevelTab? {
  for (index in stack.indices.reversed()) {
    val tab = stack[index].topLevelTabOrNull()
    if (tab != null) return tab
  }
  return null
}

/**
 * Whether a transition between two screens owned by [fromTab] and [toTab] should fade through (used
 * between top-level tabs) rather than slide (used within a tab). True only when both screens belong
 * to a tab and the tabs differ — the decision is a property of the (from, to) pair, not of a single
 * destination, since restoring a parked side-tab run lands on a deep screen that can't be classified
 * from its key alone. A transition to or from a tab-less screen (e.g. the pre-login Login screen,
 * which yields null) is never a tab change.
 */
internal fun shouldFadeThrough(fromTab: TopLevelTab?, toTab: TopLevelTab?): Boolean =
  fromTab != null && toTab != null && fromTab != toTab

/** Truncates to Home's run (Home + its drill-downs), discarding every parked side-tab run. */
internal fun collapseToHome(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val firstSideRunStart = (1..stack.lastIndex).firstOrNull { stack[it].topLevelTabOrNull() != null }
    ?: return stack.toList()
  return stack.subList(0, firstSideRunStart).toList()
}

/** The active side-tab run (the side-tab key plus its drill-downs), or empty if currently on Home. */
internal fun activeSideRun(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val start = (1..stack.lastIndex).firstOrNull { stack[it].topLevelTabOrNull() != null } ?: return emptyList()
  return stack.subList(start, stack.size).toList()
}

/** Drops the top run's drill-downs, keeping its root key. */
internal fun popTopRunToStart(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val topRunStart = stack.indexOfLast { it.topLevelTabOrNull() != null }
  if (topRunStart == -1) return stack.toList()
  return stack.subList(0, topRunStart + 1).toList()
}

/**
 * Builds the synthetic back stack for a deep-linked [key], used by [navigateUp] when the key is
 * standing alone. Reuses the existing [startDestination] tab→root map. `HomeKey` is always the
 * pinned base so the result satisfies the runs-model invariant (index 0 == HomeKey).
 */
internal fun syntheticStackFor(key: HedvigNavKey): List<HedvigNavKey> {
  val ancestry = key as? DeepLinkAncestry
  val tab = ancestry?.owningTab ?: TopLevelTab.Home
  return buildList {
    add(HomeKey)
    if (tab != TopLevelTab.Home) add(tab.startDestination)
    addAll(ancestry?.syntheticParents.orEmpty())
    add(key)
  }.distinct()
}
