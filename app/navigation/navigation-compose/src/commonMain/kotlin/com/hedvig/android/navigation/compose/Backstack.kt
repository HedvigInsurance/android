package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * A thin wrapper over the app's mutable Nav3 back stack. The [entries] list stays public and is the
 * sole source of truth; the wrapper exists so the custom navigation helpers below are discoverable
 * on a dedicated type rather than drowned in the full `MutableList` API. Features build their own
 * flow-specific extensions on top of this same receiver.
 */
@Stable
interface Backstack {
  val entries: MutableList<HedvigNavKey>

  /**
   * Up navigation. Defaults to a plain temporal pop; `:app`'s controller overrides it with
   * task-aware synthetic-stack rebuilding for lone deep links (see BackstackController).
   */
  fun navigateUp(): Boolean = popBackstack()

  /**
   * Pops the top entry. Returns false when the back stack is already at its root (nothing popped).
   *
   * This default is a pure temporal pop. `:app`'s controller overrides it so that a pop at the root
   * finishes the Activity instead of silently no-opping — i.e. Back/close from the last screen exits
   * the app. A caller that needs a different at-root behavior (e.g. jump to another tab) must branch
   * on the back stack itself rather than on this return value, since in `:app` the root case never
   * returns: it finishes.
   */
  fun popBackstack(): Boolean = Snapshot.withMutableSnapshot {
    if (entries.size <= 1) return@withMutableSnapshot false
    entries.removeAt(entries.lastIndex)
    true
  }
}

/** Pushes [key] onto the top of the back stack. */
fun Backstack.add(key: HedvigNavKey): Boolean = entries.add(key)

/** Pops up to (and optionally including) the most recent entry of [T]. No-op if absent. */
inline fun <reified T : HedvigNavKey> Backstack.popUpTo(inclusive: Boolean) {
  val index = entries.indexOfLast { it is T }
  if (index == -1) return
  val removeFrom = if (inclusive) index else index + 1
  while (entries.size > removeFrom) {
    entries.removeAt(entries.lastIndex)
  }
}

/** Pops up to (and optionally including) the most recent [T], then pushes [key]. */
inline fun <reified T : HedvigNavKey> Backstack.navigateAndPopUpTo(key: HedvigNavKey, inclusive: Boolean) {
  popUpTo<T>(inclusive)
  add(key)
}

/** Most recent back-stack entry of [T], or null. */
inline fun <reified T : HedvigNavKey> Backstack.findLastOrNull(): T? = entries.lastOrNull { it is T } as T?

/** Removes every entry of [T] from the back stack. */
inline fun <reified T : HedvigNavKey> Backstack.removeAllOf() {
  entries.removeAll { it is T }
}
