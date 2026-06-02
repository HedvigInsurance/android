package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.hedvig.android.app.ui.startDestination
import com.hedvig.android.feature.login.navigation.LoginDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph

/**
 * Owns the app's navigation back stacks for the Nav3 [androidx.navigation3.ui.NavDisplay].
 *
 * Two roots, mutually exclusive, toggled by [setLoggedIn]:
 * - logged-out: a single [loginBackStack] seeded with [LoginDestination].
 * - logged-in: one independent [SnapshotStateList] per [TopLevelGraph] tab, so each tab keeps its
 *   own drill-down depth across tab switches (the per-tab key list persists; only the currently
 *   displayed tab's entries are alive in the composition).
 *
 * [currentBackStack] is the list actually rendered. [navigator] is a single stable [Navigator] that
 * always forwards to whichever list is current, so the ~25 feature graph builders capture it once
 * and keep mutating the right stack as tabs switch.
 *
 * Note: we cannot use Nav3's `rememberNavBackStack` here — it is typed `NavBackStack<NavKey>`, while
 * the whole feature DSL (`EntryProviderScope<HedvigNavKey>`, `Navigator(MutableList<HedvigNavKey>)`)
 * is `HedvigNavKey`-typed and `NavDisplay<HedvigNavKey>` needs `List<out HedvigNavKey>`. Per-tab
 * [mutableStateListOf] of [HedvigNavKey] is the type-compatible equivalent.
 */
@Stable
internal class HedvigTopLevelBackStacks(
  private val tabBackStacks: Map<TopLevelGraph, SnapshotStateList<HedvigNavKey>>,
  private val loginBackStack: SnapshotStateList<HedvigNavKey>,
) {
  var isLoggedIn: Boolean by mutableStateOf(false)
    private set

  var currentTopLevel: TopLevelGraph by mutableStateOf(TopLevelGraph.Home)
    private set

  /** The list currently rendered by the NavDisplay. */
  val currentBackStack: SnapshotStateList<HedvigNavKey>
    get() = if (isLoggedIn) tabBackStacks.getValue(currentTopLevel) else loginBackStack

  /** The destination on top of the rendered stack — replaces Nav2's `navController.currentDestination`. */
  val currentDestination: HedvigNavKey?
    get() = currentBackStack.lastOrNull()

  /**
   * A single stable back-stack list forwarding to the active stack. Feature graphs capture this
   * once; its target changes implicitly with [currentTopLevel] / [isLoggedIn] because the forwarding
   * list resolves [currentBackStack] on every call.
   */
  val backStack: MutableList<HedvigNavKey> = ForwardingBackStack { currentBackStack }

  /** Legacy [Navigator] over the same forwarding [backStack], kept until all graphs take the list. */
  val navigator: Navigator = Navigator(backStack)

  /** Rail/bar tap: bring the tab forward, or pop it to its start key if it is already current. */
  fun selectTopLevel(topLevelGraph: TopLevelGraph) {
    if (currentTopLevel == topLevelGraph && isLoggedIn) {
      popCurrentToStart()
    } else {
      currentTopLevel = topLevelGraph
    }
  }

  private fun popCurrentToStart() {
    val stack = currentBackStack
    Snapshot.withMutableSnapshot {
      while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
  }

  /** Move into the tabbed shell. Resets every tab to its start so a re-login starts clean. */
  fun setLoggedIn() {
    Snapshot.withMutableSnapshot {
      for ((graph, stack) in tabBackStacks) {
        stack.clear()
        stack.add(graph.startDestination)
      }
      currentTopLevel = TopLevelGraph.Home
      isLoggedIn = true
    }
  }

  /** Drop back to the login root. Clears the login stack down to [LoginDestination]. */
  fun setLoggedOut() {
    Snapshot.withMutableSnapshot {
      loginBackStack.clear()
      loginBackStack.add(LoginDestination)
      isLoggedIn = false
    }
  }
}

@Composable
internal fun rememberHedvigTopLevelBackStacks(): HedvigTopLevelBackStacks {
  val tabBackStacks = TopLevelGraph.entries.associateWith { graph ->
    remember(graph) { mutableStateListOf<HedvigNavKey>(graph.startDestination) }
  }
  val loginBackStack = remember { mutableStateListOf<HedvigNavKey>(LoginDestination) }
  return remember(tabBackStacks, loginBackStack) {
    HedvigTopLevelBackStacks(tabBackStacks, loginBackStack)
  }
}

/**
 * A [MutableList] view that delegates every operation to whatever list [current] returns at call
 * time. Lets a single [Navigator] instance keep mutating the active tab's stack as the active tab
 * changes, without rebuilding the navigator (and therefore the captured graph builders).
 */
private class ForwardingBackStack(
  private val current: () -> MutableList<HedvigNavKey>,
) : MutableList<HedvigNavKey> by MutableListDelegate(current)

private class MutableListDelegate(
  private val current: () -> MutableList<HedvigNavKey>,
) : MutableList<HedvigNavKey> {
  private val target: MutableList<HedvigNavKey> get() = current()

  override val size: Int get() = target.size

  override fun contains(element: HedvigNavKey) = target.contains(element)

  override fun containsAll(elements: Collection<HedvigNavKey>) = target.containsAll(elements)

  override fun get(index: Int) = target[index]

  override fun indexOf(element: HedvigNavKey) = target.indexOf(element)

  override fun isEmpty() = target.isEmpty()

  override fun iterator() = target.iterator()

  override fun lastIndexOf(element: HedvigNavKey) = target.lastIndexOf(element)

  override fun add(element: HedvigNavKey) = target.add(element)

  override fun add(index: Int, element: HedvigNavKey) = target.add(index, element)

  override fun addAll(index: Int, elements: Collection<HedvigNavKey>) = target.addAll(index, elements)

  override fun addAll(elements: Collection<HedvigNavKey>) = target.addAll(elements)

  override fun clear() = target.clear()

  override fun listIterator() = target.listIterator()

  override fun listIterator(index: Int) = target.listIterator(index)

  override fun remove(element: HedvigNavKey) = target.remove(element)

  override fun removeAll(elements: Collection<HedvigNavKey>) = target.removeAll(elements)

  override fun removeAt(index: Int) = target.removeAt(index)

  override fun retainAll(elements: Collection<HedvigNavKey>) = target.retainAll(elements)

  override fun set(index: Int, element: HedvigNavKey) = target.set(index, element)

  override fun subList(fromIndex: Int, toIndex: Int) = target.subList(fromIndex, toIndex)
}
