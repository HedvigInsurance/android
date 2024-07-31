package com.hedvig.android.ui.claimstatus.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.HedvigPill
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
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
        ClaimPillType.Closed.NotCompensated -> stringResource(R.string.claim_decision_not_compensated)
        ClaimPillType.Closed.NotCovered -> stringResource(R.string.claim_decision_not_covered)
        ClaimPillType.Closed.Paid -> stringResource(R.string.claim_decision_paid)
      }
    }
    ClaimPillType.Open -> stringResource(R.string.home_claim_card_pill_claim)
    is ClaimPillType.PaymentAmount -> type.uiMoney.toString()
    ClaimPillType.Reopened -> stringResource(R.string.home_claim_card_pill_reopened)
    ClaimPillType.Unknown -> stringResource(R.string.home_claim_card_pill_claim)
  }
  val (color, contentColor) = when (type) {
    is ClaimPillType.Closed -> MaterialTheme.colorScheme.onSurface to MaterialTheme.colorScheme.surface
    ClaimPillType.Open -> MaterialTheme.colorScheme.outlineVariant to LocalContentColor.current
    is ClaimPillType.PaymentAmount ->
      MaterialTheme.colorScheme.infoContainer to MaterialTheme.colorScheme.onInfoContainer
    ClaimPillType.Reopened -> MaterialTheme.colorScheme.warningContainer to MaterialTheme.colorScheme.onWarningContainer
    ClaimPillType.Unknown -> MaterialTheme.colorScheme.outlineVariant to LocalContentColor.current
  }
  HedvigPill(text, color, contentColor = contentColor)
}

@HedvigPreview
@Composable
private fun PreviewClaimPillsRow() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimPillsRow(
        listOf(
          ClaimPillType.Open,
          ClaimPillType.Reopened,
          ClaimPillType.PaymentAmount(UiMoney(990.0, UiCurrencyCode.SEK)),
          ClaimPillType.Unknown,
          ClaimPillType.Closed.NotCovered,
          ClaimPillType.Closed.NotCompensated,
          ClaimPillType.Closed.Paid,
        ),
      )
    }
  }
}
