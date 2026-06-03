package com.hedvig.android.app.navigation

import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.DeepLinkAncestry
import com.hedvig.android.navigation.core.TopLevelGraph

/** Reverse of [startDestination]: the tab this key is the root of, or null if it is not a tab root. */
internal fun HedvigNavKey.topLevelGraphOrNull(): TopLevelGraph? = when (this) {
  is HomeKey -> TopLevelGraph.Home
  is InsurancesKey -> TopLevelGraph.Insurances
  is ForeverKey -> TopLevelGraph.Forever
  is PaymentsKey -> TopLevelGraph.Payments
  is ProfileKey -> TopLevelGraph.Profile
  else -> null
}

/** The tab owning the top entry: nearest tab key at or below the top. Null if the stack has no tab key. */
internal fun nearestTopLevelGraph(stack: List<HedvigNavKey>): TopLevelGraph? {
  for (index in stack.indices.reversed()) {
    val tab = stack[index].topLevelGraphOrNull()
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
internal fun shouldFadeThrough(fromTab: TopLevelGraph?, toTab: TopLevelGraph?): Boolean =
  fromTab != null && toTab != null && fromTab != toTab

/** Truncates to Home's run (Home + its drill-downs), discarding every parked side-tab run. */
internal fun collapseToHome(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val firstSideRunStart = (1..stack.lastIndex).firstOrNull { stack[it].topLevelGraphOrNull() != null }
    ?: return stack.toList()
  return stack.subList(0, firstSideRunStart).toList()
}

/** The active side-tab run (the side-tab key plus its drill-downs), or empty if currently on Home. */
internal fun activeSideRun(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val start = (1..stack.lastIndex).firstOrNull { stack[it].topLevelGraphOrNull() != null } ?: return emptyList()
  return stack.subList(start, stack.size).toList()
}

/** Drops the top run's drill-downs, keeping its root key. */
internal fun popTopRunToStart(stack: List<HedvigNavKey>): List<HedvigNavKey> {
  val topRunStart = stack.indexOfLast { it.topLevelGraphOrNull() != null }
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
  val tab = ancestry?.owningTab ?: TopLevelGraph.Home
  return buildList {
    add(HomeKey)
    if (tab != TopLevelGraph.Home) add(tab.startDestination)
    addAll(ancestry?.syntheticParents.orEmpty())
    add(key)
  }.distinct()
}
