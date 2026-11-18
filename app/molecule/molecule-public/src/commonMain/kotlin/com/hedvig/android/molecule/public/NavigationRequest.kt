@file:OptIn(ExperimentalAtomicApi::class, ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package com.hedvig.android.molecule.public

import androidx.compose.runtime.Stable
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

/**
 * A navigation to [target] that a presenter puts in its state for the platform to perform once. A request is equal
 * only to itself, so a new request for an equal target is a new state and is never dropped as a repeat.
 *
 * The platform performs it only when [tryConsume] returns true, which happens once per request, for whichever caller
 * gets there first: every copy of the state that carries the request shares it, on every thread. The presenter reads
 * [isPending] to tell whether the platform has taken it. Nothing shown on screen may depend on [isPending], since
 * taking a request does not produce a new state.
 */
@Stable
@ObjCName(name = "HedvigSharedNavigationRequest", swiftName = "NavigationRequest", exact = true)
class NavigationRequest<out T : Any> private constructor(
  /** Unique within the process, for observers that compare requests by a value. */
  val id: Long,
  val target: T,
  private val consumed: AtomicBoolean,
) {
  /** True until the platform takes the request with [tryConsume]. */
  val isPending: Boolean
    get() = !consumed.load()

  /** True for exactly one call per request. Perform the navigation only when it returns true. */
  fun tryConsume(): Boolean = consumed.compareAndSet(expectedValue = false, newValue = true)

  /**
   * The same request with [transform] applied to its target, for a platform-facing state that shows the target in
   * another type. Taking either one takes both, and they are equal.
   */
  @HiddenFromObjC
  fun <R : Any> map(transform: (T) -> R): NavigationRequest<R> = NavigationRequest(id, transform(target), consumed)

  override fun equals(other: Any?): Boolean = other is NavigationRequest<*> && other.id == id

  override fun hashCode(): Int = id.hashCode()

  /** Leaves out [target], which can carry the member's data. */
  override fun toString(): String = "NavigationRequest(id=$id, isPending=$isPending)"

  @HiddenFromObjC
  companion object {
    private val lastId = AtomicLong(0L)

    fun <T : Any> of(target: T): NavigationRequest<T> =
      NavigationRequest(lastId.incrementAndFetch(), target, AtomicBoolean(false))
  }
}

/**
 * The offer to keep when a presenter restarts from a state that held this one. A pending offer is taken and dropped,
 * so a screen still showing the state from before the restart cannot perform it either, and the member asks again. A
 * taken one is kept; it is never performed again.
 */
@HiddenFromObjC
fun <T : Any> NavigationRequest<T>?.restoredAsOffer(): NavigationRequest<T>? = this?.takeUnless { it.tryConsume() }
