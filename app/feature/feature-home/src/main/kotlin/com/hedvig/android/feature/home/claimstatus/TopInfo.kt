package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claimstatus.data.PillUiState

@Composable
internal fun TopInfo(
  pillsUiState: List<PillUiState>,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    ClaimPillsAndForwardArrow(pillsUiState)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTopInfo() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TopInfo(
        pillsUiState = PillUiState.previewList(),
        title = "All-risk",
        subtitle = "Home Insurance Renter",
      )
    }
  }
}
