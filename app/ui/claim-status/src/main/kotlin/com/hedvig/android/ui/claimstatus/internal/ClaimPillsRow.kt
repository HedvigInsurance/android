package com.hedvig.android.ui.claimstatus.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.DARK
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Claim
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.GenericClosed
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCompensated
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCovered
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.Paid
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.PaymentAmount
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Unknown
import hedvig.resources.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClaimPillsRow(pillTypes: List<ClaimPillType>, modifier: Modifier = Modifier) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    for (pillType in pillTypes) {
      ClaimPill(pillType)
    }
  }
}

@Composable
private fun ClaimPill(type: ClaimPillType) {
  val text = when (type) {
    is ClaimPillType.Closed -> {
      when (type) {
        ClaimPillType.Closed.GenericClosed -> stringResource(R.string.home_claim_card_pill_claim)
        ClaimPillType.Closed.NotCompensated -> stringResource(R.string.claim_decision_not_compensated)
        ClaimPillType.Closed.NotCovered -> stringResource(R.string.claim_decision_not_covered)
        ClaimPillType.Closed.Paid -> stringResource(R.string.claim_decision_paid)
      }
    }

    ClaimPillType.Claim -> stringResource(R.string.home_claim_card_pill_claim)
    is ClaimPillType.PaymentAmount -> type.uiMoney.toString()
    ClaimPillType.Unknown -> stringResource(R.string.home_claim_card_pill_claim)
  }
  val color: HighlightLabelDefaults.HighlightColor = when (type) {
    ClaimPillType.Claim -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
    is ClaimPillType.Closed -> {
      when (type) {
        ClaimPillType.Closed.GenericClosed -> HighlightLabelDefaults.HighlightColor.Grey(DARK)
        ClaimPillType.Closed.NotCompensated -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
        ClaimPillType.Closed.NotCovered -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
        ClaimPillType.Closed.Paid -> HighlightLabelDefaults.HighlightColor.Grey(DARK)
      }
    }

    is ClaimPillType.PaymentAmount -> HighlightLabelDefaults.HighlightColor.Blue(MEDIUM)
    ClaimPillType.Unknown -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
  }
  HighlightLabel(text, HighlightLabelDefaults.HighLightSize.Small, color)
}

@HedvigPreview
@Composable
private fun PreviewClaimPillsRow() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimPillsRow(
        listOf(
          Claim,
          PaymentAmount(UiMoney(990.0, SEK)),
          Unknown,
          NotCovered,
          NotCompensated,
          Paid,
          GenericClosed,
        ),
      )
    }
  }
}
