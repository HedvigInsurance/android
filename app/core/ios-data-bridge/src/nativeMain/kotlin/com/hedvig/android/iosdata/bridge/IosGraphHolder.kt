@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.iosdata.bridge

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/** What [IosGraphHolder] needs from the graph it holds. */
@HiddenFromObjC
interface IosReadsGraph {
  val sessionReads: SessionReads
}

/**
 * The shared Kotlin graph the iOS app holds, built on first use.
 *
 * A feature's reads reach Swift without a change here: the feature declares a facade with an `@Inject` constructor
 * taking [SessionReads], contributes an accessor interface to the app scope, and adds an extension on this class, which
 * Swift sees as a method:
 *
 * ```
 * @ContributesTo(AppScope::class)
 * interface ForeverReadsComponent { val foreverReads: ForeverReads }
 *
 * fun IosGraphHolder.foreverReads(): ForeverReads = component<ForeverReadsComponent>().foreverReads
 * ```
 *
 * and the umbrella exports the feature's module.
 */
class IosGraphHolder internal constructor(
  createGraph: () -> IosReadsGraph,
) {
  private val built: Lazy<IosReadsGraph> = lazy(createGraph)

  /** The graph, built on first use. Read it through [component]. */
  @HiddenFromObjC
  val graph: Any get() = built.value

  /**
   * A member logged in. Kotlin ends the member reads still in flight as `SESSION_ENDED`; a member read started after
   * this call runs for the new member. Logged-out reads carry on. No token passes through this call: Kotlin asks the
   * iOS app for one when it needs it. Callable from any thread, returns when done, and does nothing when the graph was
   * never used.
   */
  fun sessionStarted() {
    endMemberReads()
  }

  /** The member logged out. Kotlin ends their reads, as [sessionStarted] does. */
  fun sessionEnded() {
    endMemberReads()
  }

  private fun endMemberReads() {
    if (!built.isInitialized()) return
    built.value.sessionReads.sessionChanged()
  }
}

/** A holder over the graph [createGraph] builds on first use. */
@HiddenFromObjC
fun iosGraphHolder(createGraph: () -> IosReadsGraph): IosGraphHolder = IosGraphHolder(createGraph)

/** The part [T] of the holder's graph, for a feature's accessor extension on [IosGraphHolder]. */
@HiddenFromObjC
inline fun <reified T : Any> IosGraphHolder.component(): T = graph as T
