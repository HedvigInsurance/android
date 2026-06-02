package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Nav3 replacement for the Nav2 `navdestination`. Registers a single [NavEntry] for the [T] key.
 * The key *is* the arguments — read fields off `this: T` in [content] instead of a NavBackStackEntry.
 *
 * Per-entry transitions are supplied through [metadata] using the platform's own transition keys
 * (see the androidMain `entryTransitionMetadata` helper). `NavDisplay` reads that metadata and falls
 * back to its default transitions when absent, so plain entries need no metadata at all.
 */
inline fun <reified T : HedvigNavKey> EntryProviderScope<HedvigNavKey>.navdestination(
  metadata: Map<String, Any> = emptyMap(),
  crossinline content: @Composable T.() -> Unit,
) {
  entry<T>(metadata = metadata) { key ->
    key.content()
  }
}
