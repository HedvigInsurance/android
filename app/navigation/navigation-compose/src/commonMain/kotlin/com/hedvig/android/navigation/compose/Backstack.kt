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

  /**
   * Pops every entry above [index] so the entry at [index] becomes the new top. The stack is never
   * emptied: an [index] below 0 asks to clear the base too (e.g. an inclusive pop of a lone deep-link
   * entry with no app ancestry below it). `:app`'s controller overrides the `index < 0` case to
   * finish the App instead — same finish-at-root contract as [popBackstack].
   */
  fun popUpToIndex(index: Int) = Snapshot.withMutableSnapshot {
    val keepCount = maxOf(index + 1, 1)
    if (entries.size > keepCount) {
      entries.subList(keepCount, entries.size).clear()
    }
  }
}

/** Pushes [key] onto the top of the back stack. */
fun Backstack.add(key: HedvigNavKey): Boolean = entries.add(key)

/**
 * Pops up to (and optionally including) the most recent entry of [T]. No-op if absent.
 *
 * Delegates to [Backstack.popUpToIndex] with the index of the entry that should remain on top: [T]
 * itself when exclusive, the entry below it when inclusive. An inclusive pop of a base [T] therefore
 * targets index -1 — clearing the whole stack — which [popUpToIndex] resolves to its finish-at-root
 * behavior rather than emptying. Use [navigateAndPopUpTo] when a replacement is pushed immediately —
 * that never lands on an empty stack, so the finish is neither needed nor wanted.
 */
inline fun <reified T : HedvigNavKey> Backstack.popUpTo(inclusive: Boolean) {
  val index = entries.indexOfLast { it is T }
  if (index == -1) return
  popUpToIndex(if (inclusive) index - 1 else index)
}

/**
 * Pops up to (and optionally including) the most recent [T], then pushes [key]. Never finishes — when
 * [T] is absent nothing is popped and [key] is simply appended.
 */
inline fun <reified T : HedvigNavKey> Backstack.navigateAndPopUpTo(key: HedvigNavKey, inclusive: Boolean) {
  val index = entries.indexOfLast { it is T }
  val removeFrom = when {
    index == -1 -> entries.size
    inclusive -> index
    else -> index + 1
  }
  Snapshot.withMutableSnapshot {
    if (entries.size > removeFrom) {
      entries.subList(removeFrom, entries.size).clear()
    }
    entries.add(key)
  }
}

/** Most recent back-stack entry of [T], or null. */
inline fun <reified T : HedvigNavKey> Backstack.findLastOrNull(): T? = entries.lastOrNull { it is T } as T?

/** Removes every entry of [T] from the back stack. */
inline fun <reified T : HedvigNavKey> Backstack.removeAllOf() {
  entries.removeAll { it is T }
}
