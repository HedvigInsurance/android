package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.HedvigDateTimeFormatterDefaults
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.ui.claimstatus.internal.ClaimPillsRow
import com.hedvig.android.ui.claimstatus.internal.ClaimProgressRow
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
fun ClaimStatusCard(
  uiState: ClaimStatusCardUiState,
  onClick: ((claimId: String) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = if (onClick != null) {
      { onClick.invoke(uiState.id) }
    } else {
      null
    },
    modifier = modifier,
  ) {
    ClaimStatusCardContent(uiState)
  }
}

@Composable
fun ClaimStatusCardContent(uiState: ClaimStatusCardUiState) {
  Column(Modifier.padding(16.dp)) {
    ClaimPillsRow(pillTypes = uiState.pillTypes)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = uiState.claimType?.lowercase()?.replaceFirstChar { it.uppercase() }
        ?: stringResource(R.string.claim_casetype_insurance_case),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(horizontal = 2.dp),
    )
    val subtext = if (uiState.insuranceDisplayName != null) {
      uiState.insuranceDisplayName
    } else {
      val formattedDate = HedvigDateTimeFormatterDefaults
        .monthDateAndYear(getLocale())
        .format(uiState.submittedDate.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
      "${stringResource(R.string.claim_status_detail_submitted)} $formattedDate"
    }
    Text(
      text = subtext,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 2.dp),
    )
    Spacer(Modifier.height(16.dp))
    ClaimProgressRow(claimProgressItemsUiState = uiState.claimProgressItemsUiState)
  }
}

@HedvigPreview
@Preview(locale = "sv")
@Composable
private fun PreviewClaimStatusCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      val claimStatusData = ClaimStatusCardUiState(
        id = "id",
        pillTypes = listOf(ClaimPillType.Open, ClaimPillType.Closed.NotCompensated),
        claimProgressItemsUiState = listOf(
          ClaimProgressSegment(ClaimProgressSegment.SegmentText.Closed, ClaimProgressSegment.SegmentType.PAID),
        ),
        claimType = "Broken item",
        insuranceDisplayName = null,
        submittedDate = Instant.parse("2024-05-31T00:00:50Z"),
      )
      ClaimStatusCard(claimStatusData, {})
    }
  }
}
