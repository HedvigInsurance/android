package com.hedvig.feature.claim.chat.ui.outcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import hedvig.resources.CHAT_CONVERSATION_CLAIM_TITLE
import hedvig.resources.CLAIMS_SUCCESS_TITLE
import hedvig.resources.Res
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

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
