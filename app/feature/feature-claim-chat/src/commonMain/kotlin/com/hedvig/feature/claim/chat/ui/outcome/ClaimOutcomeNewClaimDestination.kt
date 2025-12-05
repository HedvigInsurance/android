package com.hedvig.feature.claim.chat.ui.outcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import hedvig.resources.CHAT_CONVERSATION_CLAIM_TITLE
import hedvig.resources.CLAIMS_SUCCESS_TITLE
import hedvig.resources.Res
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ClaimOutcomeNewClaimDestination(claim: ClaimIntentOutcome.Claim, navigateToClaimDetails: () -> Unit) {
  val locale = getLocale()
  val dateFormatter = remember(locale) {
    HedvigDateTimeFormatterDefaults.yearMonthDateAndTime(locale)
  }
  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    HedvigText(stringResource(Res.string.CLAIMS_SUCCESS_TITLE))
    val formattedDate = dateFormatter.format(
      claim.claimSubmissionDate.toLocalDateTime(TimeZone.currentSystemDefault()),
    )
    HedvigText("Submitted at: $formattedDate")
    HedvigButton(
      onClick = dropUnlessResumed { navigateToClaimDetails() },
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
      text = stringResource(Res.string.CHAT_CONVERSATION_CLAIM_TITLE),
      modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimOutcomeNewClaimDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimOutcomeNewClaimDestination(
        ClaimIntentOutcome.Claim("123", Instant.parse("2024-05-01T00:00:00Z")),
        {},
      )
    }
  }
}
