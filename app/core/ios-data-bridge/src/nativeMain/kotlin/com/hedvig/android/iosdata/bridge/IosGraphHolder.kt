@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.iosdata.bridge

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * The shared Kotlin graph the iOS app holds, built on first use.
 *
 * A feature reaches Swift from its own module, without a change here or in the graph. It writes a class of plain
 * suspend functions annotated `@NativeCoroutines` (the module applies the `com.rickclephas.kmp.nativecoroutines`
 * plugin), contributes an accessor for it, and adds an extension on this class, which Swift sees as a method:
 *
 * ```
 * @Inject
 * class ForeverReads(private val apolloClient: ApolloClient) {
 *   @NativeCoroutines
 *   suspend fun referrals(): ForeverInfo = iosRead {
 *     apolloClient.query(FullReferralsQuery()).safeExecute().bind().toForeverInfo()
 *   }
 * }
 *
 * @ContributesTo(AppScope::class)
 * interface ForeverReadsComponent { val foreverReads: ForeverReads }
 *
 * fun IosGraphHolder.foreverReads(): ForeverReads = component<ForeverReadsComponent>().foreverReads
 * ```
 *
 * The umbrella exports the feature's module, and Swift calls
 * `try await asyncFunction(for: holder.foreverReads().referrals())`. A read made before login injects
 * `@PublicApolloClient ApolloClient`, which never sends a token.
 */
class IosGraphHolder internal constructor(createGraph: () -> Any) {
  private val built: Lazy<Any> = lazy(createGraph)

  /** The graph, built on first use. Read it through [component]. */
  @HiddenFromObjC
  val graph: Any get() = built.value
}

/** A holder over the graph [createGraph] builds on first use. */
@HiddenFromObjC
fun iosGraphHolder(createGraph: () -> Any): IosGraphHolder = IosGraphHolder(createGraph)

/** The part [T] of the holder's graph, for a feature's accessor extension on [IosGraphHolder]. */
@HiddenFromObjC
inline fun <reified T : Any> IosGraphHolder.component(): T = graph as T
