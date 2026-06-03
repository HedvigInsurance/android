package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Stable
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
}

/** Pushes [key] onto the top of the back stack. */
fun Backstack.add(key: HedvigNavKey): Boolean = entries.add(key)

/** Pops the top entry. Returns false if the back stack is at its root (nothing popped). */
fun Backstack.popBackstack(): Boolean {
  if (entries.size <= 1) return false
  entries.removeAt(entries.lastIndex)
  return true
}

/** TODO nav3: Make this equivalent to Nav2 navigateUp() in deep link scenarios. */
fun Backstack.navigateUp(): Boolean = popBackstack()

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
