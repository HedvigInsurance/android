package com.hedvig.feature.claim.chat.ui.outcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome

@Composable
internal fun ClaimOutcomeDeflectDestination(
  deflect: ClaimIntentOutcome.Deflect,
  modifier: Modifier = Modifier,
) {
  HedvigText("Deflect")
}

@HedvigPreview
@Composable
private fun PreviewClaimOutcomeDeflectDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimOutcomeDeflectDestination(
        deflect = ClaimIntentOutcome.Deflect(
          title = "title",
          infoText = "infoText",
          warningText = "warningText",
          partners = emptyList(),
          partnersInfo = ClaimIntentOutcome.Deflect.InfoBlock("title", "description"),
          content = ClaimIntentOutcome.Deflect.InfoBlock("title", "description"),
          faq = emptyList()
        ),
      )
    }
  }
}
