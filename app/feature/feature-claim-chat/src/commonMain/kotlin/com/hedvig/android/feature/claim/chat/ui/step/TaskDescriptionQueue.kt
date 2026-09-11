package com.hedvig.android.feature.claim.chat.ui.step

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Walks [descriptions] one at a time, holding each for at least [minimumDwell] before moving on.
 *
 * A single poll can deliver several descriptions at once. Rendering only the most recent one would skip the ones in
 * between, so this advances through the backlog in order rather than jumping to the end.
 *
 * [descriptions] is expected to only grow, which is how the presenter accumulates them.
 */
internal fun pacedDescriptions(descriptions: Flow<List<String>>, minimumDwell: Duration): Flow<String> = channelFlow {
  val known = MutableStateFlow(emptyList<String>())
  launch { descriptions.collect { known.value = it } }
  var shown = 0
  while (true) {
    // Suspends until something unshown exists, so an idle task step costs nothing.
    val available = known.first { it.size > shown }
    send(available[shown])
    shown++
    delay(minimumDwell)
  }
}

/** Returns the description that should currently be on screen, or null before the first one arrives. */
@Composable
internal fun rememberPacedDescription(
  descriptions: List<String>,
  minimumDwell: Duration = defaultMinimumDwell,
): String? {
  val latest by rememberUpdatedState(descriptions)
  return produceState<String?>(initialValue = null) {
    pacedDescriptions(snapshotFlow { latest }, minimumDwell).collect { value = it }
  }.value
}

private val defaultMinimumDwell = 900.milliseconds
