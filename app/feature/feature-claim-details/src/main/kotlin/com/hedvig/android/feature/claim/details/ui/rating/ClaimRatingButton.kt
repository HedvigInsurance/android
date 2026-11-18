package com.hedvig.android.feature.claim.details.ui.rating

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

internal enum class RatingButtonState {
  Enabled,
  Submitted,
}

@Composable
internal fun ClaimRatingButton(state: RatingButtonState, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val isEnabled = state == RatingButtonState.Enabled
  HedvigCard(onClick = onClick.takeIf { isEnabled }, modifier = modifier.fillMaxWidth()) {
    HedvigText(
      text = when (state) {
        // TODO: Add "Rate your claim" / "Betygsätt ditt ärende" to Lokalise
        RatingButtonState.Enabled -> "Rate your claim"

        // TODO: Add "Rating submitted" / "Betyg skickat" to Lokalise
        RatingButtonState.Submitted -> "Rating submitted"
      },
      color = if (isEnabled) Color.Unspecified else HedvigTheme.colorScheme.textDisabled,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimRatingButtonEnabled() {
  PreviewButton(RatingButtonState.Enabled)
}

@HedvigPreview
@Composable
private fun PreviewClaimRatingButtonSubmitted() {
  PreviewButton(RatingButtonState.Submitted)
}

@Composable
private fun PreviewButton(state: RatingButtonState) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimRatingButton(state = state, onClick = {}, modifier = Modifier.padding(16.dp))
    }
  }
}
