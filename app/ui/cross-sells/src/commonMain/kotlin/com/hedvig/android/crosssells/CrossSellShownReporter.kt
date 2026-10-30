package com.hedvig.android.crosssells

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChanged

/**
 * Reports each offer at most once for as long as it stays in composition, so remember it where a visit to the surface
 * starts and ends: at the root of a screen for a list, inside the content of a sheet. Remembering it per row would
 * report an offer again whenever a lazy list scrolls it back in.
 */
@Composable
fun rememberCrossSellShownReporter(onShown: (offerId: String, offerUrl: String) -> Unit): CrossSellShownReporter {
  val currentOnShown by rememberUpdatedState(onShown)
  return remember { CrossSellShownReporter { offerId, offerUrl -> currentOnShown(offerId, offerUrl) } }
}

@Stable
class CrossSellShownReporter internal constructor(
  private val onShown: (offerId: String, offerUrl: String) -> Unit,
) {
  private val reportedOfferIds = mutableSetOf<String>()

  internal fun report(offerId: String, offerUrl: String) {
    if (reportedOfferIds.add(offerId)) onShown(offerId, offerUrl)
  }
}

/** An offer counts as shown once at least half of it has been on screen for half a second. */
fun Modifier.reportCrossSellShown(reporter: CrossSellShownReporter, offerId: String, offerUrl: String): Modifier =
  onVisibilityChanged(minDurationMs = 500, minFractionVisible = 0.5f) { visible ->
    if (visible) reporter.report(offerId, offerUrl)
  }
