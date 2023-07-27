package com.hedvig.android.feature.home.claimstatus.claimprogress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ContentAlpha
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusColors

@Composable
fun ClaimProgressRow(
  claimProgressItemsUiState: List<ClaimProgressUiState>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    claimProgressItemsUiState.forEach { claimProgressUiState: ClaimProgressUiState ->
      ClaimProgress(
        text = claimProgressUiState.text,
        type = claimProgressUiState.type,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ClaimProgress(
  text: String,
  type: ClaimProgressUiState.ClaimProgressType,
  modifier: Modifier,
) {
  val color = when (type) {
    ClaimProgressUiState.ClaimProgressType.PAID -> ClaimStatusColors.Progress.paid
    ClaimProgressUiState.ClaimProgressType.REOPENED -> ClaimStatusColors.Progress.reopened
    ClaimProgressUiState.ClaimProgressType.UNKNOWN,
    ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE,
    ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE,
    ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE,
    -> MaterialTheme.colorScheme.primary
  }
  val contentAlpha = when (type) {
    ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE -> ContentAlpha.MEDIUM
    ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE -> ContentAlpha.DISABLED
    ClaimProgressUiState.ClaimProgressType.PAID -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.REOPENED -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.UNKNOWN -> ContentAlpha.HIGH
  }
  ClaimProgress(
    text = text,
    color = color,
    contentAlpha = contentAlpha,
    modifier = modifier,
  )
}

@Composable
private fun ClaimProgress(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
  contentAlpha: ContentAlpha = ContentAlpha.HIGH,
) {
  CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = contentAlpha.value)) {
    Column(modifier = modifier) {
      val progressColor = color.copy(alpha = contentAlpha.value)
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp),
      ) {
        drawRect(progressColor)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimProgressRow() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimProgressRow(
        listOf(
          ClaimProgressUiState(
            "Submitted",
            ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE,
          ),
          ClaimProgressUiState(
            "Being Handled",
            ClaimProgressUiState.ClaimProgressType.REOPENED,
          ),
          ClaimProgressUiState(
            "Closed",
            ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE,
          ),
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimProgress() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimProgress(
        "Text",
        MaterialTheme.colorScheme.primary,
      )
    }
  }
}
