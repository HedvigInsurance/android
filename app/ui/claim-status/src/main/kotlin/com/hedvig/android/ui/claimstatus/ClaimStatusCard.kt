package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.ui.claimstatus.internal.ClaimPillsRow
import com.hedvig.android.ui.claimstatus.internal.ClaimProgressRow
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import hedvig.resources.R

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
    Column {
      Column(modifier = Modifier.padding(16.dp)) {
        ClaimPillsRow(pillTypes = uiState.pillTypes)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(R.string.claim_casetype_insurance_case),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(horizontal = 2.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
      }
      HorizontalDivider()
      ClaimProgressRow(
        claimProgressItemsUiState = uiState.claimProgressItemsUiState,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

@HedvigPreview
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
      )
      ClaimStatusCard(claimStatusData, null)
    }
  }
}
