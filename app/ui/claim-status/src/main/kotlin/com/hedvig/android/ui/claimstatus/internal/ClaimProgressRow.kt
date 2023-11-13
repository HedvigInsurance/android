package com.hedvig.android.ui.claimstatus.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import hedvig.resources.R

@Composable
internal fun ClaimProgressRow(claimProgressItemsUiState: List<ClaimProgressSegment>, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    for (claimProgressSegment in claimProgressItemsUiState) {
      ClaimProgress(
        segmentText = claimProgressSegment.text,
        type = claimProgressSegment.type,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ClaimProgress(
  segmentText: ClaimProgressSegment.SegmentText,
  type: ClaimProgressSegment.SegmentType,
  modifier: Modifier = Modifier,
) {
  val color = when (type) {
    ClaimProgressSegment.SegmentType.PAID -> MaterialTheme.colorScheme.infoElement
    ClaimProgressSegment.SegmentType.REOPENED -> MaterialTheme.colorScheme.warningElement
    ClaimProgressSegment.SegmentType.CLOSED,
    ClaimProgressSegment.SegmentType.PAST_INACTIVE,
    ClaimProgressSegment.SegmentType.CURRENTLY_ACTIVE,
    ClaimProgressSegment.SegmentType.FUTURE_INACTIVE,
    ClaimProgressSegment.SegmentType.UNKNOWN,
    -> MaterialTheme.colorScheme.primary
  }
  val contentAlpha = when (type) {
    ClaimProgressSegment.SegmentType.PAID -> ContentAlpha.HIGH
    ClaimProgressSegment.SegmentType.REOPENED -> ContentAlpha.HIGH
    ClaimProgressSegment.SegmentType.CLOSED -> ContentAlpha.HIGH
    ClaimProgressSegment.SegmentType.PAST_INACTIVE -> ContentAlpha.MEDIUM
    ClaimProgressSegment.SegmentType.CURRENTLY_ACTIVE -> ContentAlpha.HIGH
    ClaimProgressSegment.SegmentType.FUTURE_INACTIVE -> ContentAlpha.DISABLED
    ClaimProgressSegment.SegmentType.UNKNOWN -> ContentAlpha.HIGH
  }
  val text = when (segmentText) {
    ClaimProgressSegment.SegmentText.Submitted -> stringResource(R.string.claim_status_detail_submitted)
    ClaimProgressSegment.SegmentText.BeingHandled -> stringResource(R.string.claim_status_bar_being_handled)
    ClaimProgressSegment.SegmentText.Closed -> stringResource(R.string.claim_status_detail_closed)
  }
  ClaimProgress(
    text = text,
    color = color,
    contentAlpha = contentAlpha,
    modifier = modifier,
  )
}

@Composable
private fun ClaimProgress(text: String, color: Color, contentAlpha: ContentAlpha, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    val progressColor = color.copy(alpha = contentAlpha.value)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(CircleShape)
        .background(progressColor),
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = text,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
      color = LocalContentColor.current.copy(alpha = contentAlpha.value),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

private enum class ContentAlpha {
  HIGH,
  MEDIUM,
  DISABLED,
  ;

  val value: Float
    @Composable
    get() = when (this) {
      HIGH -> 1f
      MEDIUM -> 0.74f
      DISABLED -> DisabledAlpha
    }
}

@HedvigPreview
@Composable
private fun PreviewClaimProgressRow() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimProgressRow(
        listOf(
          ClaimProgressSegment(
            ClaimProgressSegment.SegmentText.Submitted,
            ClaimProgressSegment.SegmentType.PAST_INACTIVE,
          ),
          ClaimProgressSegment(
            ClaimProgressSegment.SegmentText.BeingHandled,
            ClaimProgressSegment.SegmentType.REOPENED,
          ),
          ClaimProgressSegment(
            ClaimProgressSegment.SegmentText.Closed,
            ClaimProgressSegment.SegmentType.FUTURE_INACTIVE,
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
      for (segmentType in ClaimProgressSegment.SegmentType.entries) {
        ClaimProgress(
          segmentText = ClaimProgressSegment.SegmentText.Closed,
          type = segmentType,
        )
      }
    }
  }
}
