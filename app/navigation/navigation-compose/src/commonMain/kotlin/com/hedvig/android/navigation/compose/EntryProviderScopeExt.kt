package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import com.hedvig.android.navigation.common.Destination
import kotlin.reflect.KClass

/**
 * Nav3 replacement for the Nav2 `navdestination`. Registers a single [NavEntry] for the [T] key.
 * The key *is* the arguments — read fields off `this: T` in [content] instead of a NavBackStackEntry.
 *
 * Per-entry transitions are supplied through [metadata] using the platform's own transition keys
 * (see the androidMain `entryTransitionMetadata` helper). `NavDisplay` reads that metadata and falls
 * back to its default transitions when absent, so plain entries need no metadata at all.
 */
inline fun <reified T : Destination> EntryProviderScope<Destination>.navdestination(
  metadata: Map<String, Any> = emptyMap(),
  crossinline content: @Composable T.() -> Unit,
) {
  entry<T>(metadata = metadata) { key ->
    key.content()
  }
}

/**
 * Nav3 replacement for the Nav2 `navgraph`. Nav3 back stacks are flat — there is no nested graph
 * container — so this is a plain grouping function: it just invokes [builder] to register the
 * child entries into the same flat provider. [startDestination] is kept for call-site parity and
 * documents which key seeds the flow; the back stack itself decides the actual start.
 */
inline fun EntryProviderScope<Destination>.navgraph(
  @Suppress("UNUSED_PARAMETER") startDestination: KClass<out Destination>,
  builder: EntryProviderScope<Destination>.() -> Unit,
) {
  builder()
}
