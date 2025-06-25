package com.hedvig.android.ui.claimstatus.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.DARK
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getDescription
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
        GenericClosed -> stringResource(R.string.claim_status_detail_closed)
        NotCompensated -> stringResource(R.string.claim_decision_not_compensated)
        NotCovered -> stringResource(R.string.claim_decision_not_covered)
        Paid -> stringResource(R.string.claim_decision_paid)
        ClaimPillType.Closed.Unresponsive -> stringResource(R.string.claim_decision_unresponsive)
      }
    }

    ClaimPillType.Claim -> stringResource(R.string.home_claim_card_pill_claim)
    is ClaimPillType.PaymentAmount -> type.uiMoney.toString()
    ClaimPillType.Unknown -> stringResource(R.string.home_claim_card_pill_claim)
  }
  val voiceDescription = when (type) {
    is ClaimPillType.Closed, ClaimPillType.Claim, ClaimPillType.Unknown -> text
    is ClaimPillType.PaymentAmount -> type.uiMoney.getDescription()
  }
  val color: HighlightLabelDefaults.HighlightColor = when (type) {
    ClaimPillType.Claim -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
    is ClaimPillType.Closed -> {
      when (type) {
        GenericClosed -> Grey(DARK)
        NotCompensated -> Grey(MEDIUM, true)
        NotCovered -> Grey(MEDIUM, true)
        Paid -> Grey(DARK)
        ClaimPillType.Closed.Unresponsive -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
      }
    }

    is ClaimPillType.PaymentAmount -> HighlightLabelDefaults.HighlightColor.Blue(MEDIUM)
    ClaimPillType.Unknown -> HighlightLabelDefaults.HighlightColor.Grey(MEDIUM, true)
  }
  HighlightLabel(
    text,
    HighlightLabelDefaults.HighLightSize.Small,
    color,
    modifier = Modifier.semantics {
      contentDescription = voiceDescription
    },
  )
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
