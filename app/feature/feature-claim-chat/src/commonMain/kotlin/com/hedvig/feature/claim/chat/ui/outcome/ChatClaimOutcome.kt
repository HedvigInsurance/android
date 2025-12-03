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
internal fun ClaimIntentOutcomeDestination(
  outcome: ClaimIntentOutcome,
  onNavigateToClaim: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (outcome) {
    is ClaimIntentOutcome.Claim -> {
      Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        HedvigText(stringResource(Res.string.CLAIMS_SUCCESS_TITLE))
        Spacer(Modifier.height(16.dp))
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          HedvigButton(
            onClick = {
              onNavigateToClaim(outcome.claimId)
            },
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            buttonSize = ButtonDefaults.ButtonSize.Medium,
            text = stringResource(Res.string.CHAT_CONVERSATION_CLAIM_TITLE),
          )
        }
      }
    }

    is ClaimIntentOutcome.Deflect -> {
      HedvigText("Deflect")
    }

    ClaimIntentOutcome.Unknown -> {
      HedvigText("Unknown")
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimIntentOutcome() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimIntentOutcomeDestination(
        ClaimIntentOutcome.Claim("123", Instant.parse("2024-05-01T00:00:00Z")),
        {},
      )
    }
  }
}
