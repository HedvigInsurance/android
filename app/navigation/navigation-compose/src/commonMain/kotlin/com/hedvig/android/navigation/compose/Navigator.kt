package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Stable
import com.hedvig.android.navigation.common.Destination
import kotlin.reflect.KClass

/**
 * Thin abstraction over the Nav3 back-stack list used for *in-flow* navigation. The ~25 feature
 * graph builders depend on this instead of touching the raw [MutableList]. It deliberately does
 * NOT model the top-level-tab save/restore semantics (launchSingleTop/saveState/restoreState) —
 * those live in the multi-tab back-stack holder.
 */
@Stable
interface Navigator {
  fun navigate(destination: Destination)

  /**
   * Navigates to [destination] after popping up to the most recent entry of [popUpTo]. Replaces the
   * Nav2 `navigate(route, navOptions { popUpTo<T> { inclusive } })` pattern for in-flow navigation.
   */
  fun navigate(destination: Destination, popUpTo: KClass<out Destination>, inclusive: Boolean)

  /** Pops the top entry. Returns false if the back stack is at its root (nothing popped). */
  fun popBackStack(): Boolean

  /** Equivalent to Nav2 navigateUp(). */
  fun navigateUp(): Boolean

  /**
   * Pops up to (and optionally including) the most recent entry of [klass]. Replaces Nav2
   * `typedPopBackStack<T>(inclusive)` / `navOptions { popUpTo<T> { inclusive } }`.
   */
  fun popUpTo(klass: KClass<out Destination>, inclusive: Boolean)

  /** Removes every entry of [klass] from the back stack. Replaces Nav2 `typedClearBackStack`. */
  fun clearBackStackOf(klass: KClass<out Destination>)

  /**
   * Returns the most recent back-stack entry of [klass], or null if none. Replaces Nav2
   * `getRouteFromBackStackOrNull<T>()` — in Nav3 the key *is* the arguments.
   */
  fun <T : Destination> findLastOrNull(klass: KClass<T>): T?
}

inline fun <reified T : Destination> Navigator.popUpTo(inclusive: Boolean) = popUpTo(T::class, inclusive)

inline fun <reified T : Destination> Navigator.navigate(destination: Destination, inclusive: Boolean) =
  navigate(destination, T::class, inclusive)

inline fun <reified T : Destination> Navigator.clearBackStackOf() = clearBackStackOf(T::class)

inline fun <reified T : Destination> Navigator.findLastOrNull(): T? = findLastOrNull(T::class)

fun Navigator(backStack: MutableList<Destination>): Navigator = NavigatorImpl(backStack)

@Stable
internal class NavigatorImpl(
  private val backStack: MutableList<Destination>,
) : Navigator {
  override fun navigate(destination: Destination) {
    backStack.add(destination)
  }

  override fun navigate(destination: Destination, popUpTo: KClass<out Destination>, inclusive: Boolean) {
    popUpTo(popUpTo, inclusive)
    backStack.add(destination)
  }

  override fun popBackStack(): Boolean {
    if (backStack.size <= 1) return false
    backStack.removeAt(backStack.lastIndex)
    return true
  }

  override fun navigateUp(): Boolean = popBackStack()

  override fun popUpTo(klass: KClass<out Destination>, inclusive: Boolean) {
    val index = backStack.indexOfLast { klass.isInstance(it) }
    if (index == -1) return
    val removeFrom = if (inclusive) index else index + 1
    while (backStack.size > removeFrom) {
      backStack.removeAt(backStack.lastIndex)
    }
  }

  override fun clearBackStackOf(klass: KClass<out Destination>) {
    backStack.removeAll { klass.isInstance(it) }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : Destination> findLastOrNull(klass: KClass<T>): T? =
    backStack.lastOrNull { klass.isInstance(it) } as T?
}
