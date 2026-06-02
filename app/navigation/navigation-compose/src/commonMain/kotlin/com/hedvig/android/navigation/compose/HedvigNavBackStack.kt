package com.hedvig.android.navigation.compose

import com.hedvig.android.navigation.common.HedvigNavKey

/** Pops the top entry. Returns false if the back stack is at its root (nothing popped). */
fun MutableList<HedvigNavKey>.popBackStack(): Boolean {
  if (size <= 1) return false
  removeAt(lastIndex)
  return true
}

/** Equivalent to Nav2 navigateUp(). */
fun MutableList<HedvigNavKey>.navigateUp(): Boolean = popBackStack()

/** Pops up to (and optionally including) the most recent entry of [T]. No-op if absent. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.popUpTo(inclusive: Boolean) {
  val index = indexOfLast { it is T }
  if (index == -1) return
  val removeFrom = if (inclusive) index else index + 1
  while (size > removeFrom) {
    removeAt(lastIndex)
  }
}

/** Pops up to (and optionally including) the most recent [T], then pushes [key]. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.navigateAndPopUpTo(
  key: HedvigNavKey,
  inclusive: Boolean,
) {
  popUpTo<T>(inclusive)
  add(key)
}

/** Most recent back-stack entry of [T], or null. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.findLastOrNull(): T? = lastOrNull { it is T } as T?

/** Removes every entry of [T] from the back stack. */
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.removeAllOf() {
  removeAll { it is T }
}
