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
 * [isPending] to tell whether the platform has taken it, in event handlers, effects and when restoring. Nothing shown
 * on screen, and no value computed while composing, may depend on [isPending], since taking a request does not produce
 * a new state and is not observed by composition.
 *
 * A presenter that starts from a last state holding a request passes it through the restore function for its kind:
 * [restoredAsTapOffer], [restoredAsWriteOutcome] or [restoredAsRedirect]. Each kind needs a different rule, so there
 * is no default.
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
 * Restores an offer the member can repeat with one tap, such as the next step after checking local input. A pending
 * offer is taken and dropped, since a restart can come long after the tap and the member taps again; taking it means a
 * screen still showing the old state cannot perform it either. A taken one is kept and never performed again.
 *
 * Not for anything that follows a write: dropping it makes the member write again.
 */
@HiddenFromObjC
fun <T : Any> NavigationRequest<T>?.restoredAsTapOffer(): NavigationRequest<T>? = this?.takeUnless { it.tryConsume() }

/**
 * Restores a navigation that reports the outcome of a write, such as a success exit or a failure screen. It is kept as
 * it was, pending or taken, because the write already happened: dropping it would hide its outcome or make the member
 * send it again.
 */
@HiddenFromObjC
fun <R : NavigationRequest<*>?> R.restoredAsWriteOutcome(): R = this

/**
 * Restores a redirect the presenter made by itself, such as skipping a choice with only one option. A pending one is
 * kept, since nobody repeats a redirect. A taken one is dropped: the screen that took it navigated away, so a screen
 * showing it again was left behind (for example a second entry sharing the ViewModel) and would wait on it forever.
 * The presenter treats null as "no redirect yet" and computes it again, usually by loading, which redirects with a new
 * request.
 */
@HiddenFromObjC
fun <T : Any> NavigationRequest<T>?.restoredAsRedirect(): NavigationRequest<T>? = this?.takeIf { it.isPending }
