package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.ui.claimstatus.internal.ClaimPillsRow
import com.hedvig.android.ui.claimstatus.internal.ClaimProgressRow
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Claim
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCompensated
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Closed
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
fun ClaimStatusCard(
  uiState: ClaimStatusCardUiState,
  onClick: (claimId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val description = stringResource(R.string.TALKBACK_CLAIM_STATUS_CARD)
  HedvigCard(
    onClick = { onClick(uiState.id) },
    modifier = modifier
      .semantics {
        contentDescription = description
      },
  ) {
    Column {
      ClaimStatusCardContent(uiState, withInfoIcon = true, Modifier.padding(16.dp))
      HedvigButton(
        text = stringResource(R.string.claim_status_claim_details_button),
        onClick = { onClick(uiState.id) },
        enabled = true,
        buttonStyle = Secondary,
        buttonSize = Medium,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
fun ClaimStatusCardContent(uiState: ClaimStatusCardUiState, withInfoIcon: Boolean, modifier: Modifier = Modifier) {
  Column(modifier) {
    Row {
      ClaimPillsRow(pillTypes = uiState.pillTypes, modifier = Modifier.weight(1f))
      if (withInfoIcon) {
        Icon(HedvigIcons.InfoOutline, null, Modifier.size(24.dp), HedvigTheme.colorScheme.fillSecondary)
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HedvigText(
      text = uiState.claimType?.lowercase()?.replaceFirstChar { it.uppercase() }
        ?: stringResource(R.string.claim_casetype_insurance_case),
      style = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 2.dp),
    )
    val subtext = if (uiState.insuranceDisplayName != null) {
      uiState.insuranceDisplayName
    } else {
      val formattedDate = HedvigDateTimeFormatterDefaults
        .dateMonthAndYear(getLocale())
        .format(
          uiState.submittedDate.toLocalDateTime(
            TimeZone.currentSystemDefault(),
          ).toJavaLocalDateTime(),
        )
      "${stringResource(R.string.claim_status_detail_submitted)} $formattedDate"
    }
    HedvigText(
      text = subtext,
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.padding(horizontal = 2.dp),
    )
    Spacer(Modifier.height(18.dp))
    ClaimProgressRow(claimProgressItemsUiState = uiState.claimProgressItemsUiState)
  }
}

@HedvigPreview
@Preview(locale = "sv")
@Composable
private fun PreviewClaimStatusCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val claimStatusData = ClaimStatusCardUiState(
        id = "id",
        pillTypes = listOf(Claim, NotCompensated),
        claimProgressItemsUiState = listOf(
          ClaimProgressSegment(Closed, INACTIVE),
        ),
        claimType = "Broken item",
        insuranceDisplayName = null,
        submittedDate = Instant.parse("2024-05-31T00:00:50Z"),
      )
      ClaimStatusCard(claimStatusData, {})
    }
  }
}
