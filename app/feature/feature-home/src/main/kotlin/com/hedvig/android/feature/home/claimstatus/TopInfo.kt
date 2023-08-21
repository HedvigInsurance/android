package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    Spacer(modifier = Modifier.height(20.dp))
    Text(title)
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.body2,
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTopInfo() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      TopInfo(
        pillsUiState = PillUiState.previewList(),
        title = "All-risk",
        subtitle = "Home Insurance Renter",
      )
    }
  }
}
