package com.hedvig.android.feature.claim.details.ui.rating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Calls [openRating] once, the first time [ratingButton] is known, if the claim can still be rated. The handled flag is
 * saved state, so a screen restored after process death does not open the sheet a second time.
 */
@Composable
internal fun AutoOpenRatingEffect(openRatingOnLoad: Boolean, ratingButton: RatingButtonState?, openRating: () -> Unit) {
  var isHandled by rememberSaveable { mutableStateOf(!openRatingOnLoad) }
  val currentOpenRating by rememberUpdatedState(openRating)
  LaunchedEffect(isHandled, ratingButton) {
    if (isHandled || ratingButton == null) return@LaunchedEffect
    isHandled = true
    if (ratingButton == RatingButtonState.Enabled) {
      currentOpenRating()
    }
  }
}
