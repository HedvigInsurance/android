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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.BeingHandled
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Closed
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Submitted
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.ACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.UNKNOWN
import hedvig.resources.R

@Composable
internal fun ClaimProgressRow(claimProgressItemsUiState: List<ClaimProgressSegment>, modifier: Modifier = Modifier) {
  val currentStatus =
    claimProgressItemsUiState.lastOrNull { it.type == ACTIVE }?.text ?: stringResource(R.string.TALKBACK_UNKNOWN)
  val description = stringResource(R.string.TALKBACK_CLAIM_STATUS, currentStatus)
  Row(
    modifier = modifier.semantics {
      contentDescription = description
    },
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    for (claimProgressSegment in claimProgressItemsUiState) {
      ClaimProgress(
        segmentText = claimProgressSegment.text,
        type = claimProgressSegment.type,
        modifier = Modifier.weight(1f).semantics {
          hideFromAccessibility()
        },
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
    ClaimProgressSegment.SegmentType.ACTIVE -> HedvigTheme.colorScheme.signalGreenElement
    ClaimProgressSegment.SegmentType.INACTIVE -> HedvigTheme.colorScheme.fillDisabled
    ClaimProgressSegment.SegmentType.UNKNOWN -> HedvigTheme.colorScheme.signalGreenElement
  }
  val textColor = when (type) {
    ACTIVE -> HedvigTheme.colorScheme.textPrimary
    INACTIVE -> HedvigTheme.colorScheme.textPrimary
    UNKNOWN -> HedvigTheme.colorScheme.textTertiary
  }
  val text = when (segmentText) {
    ClaimProgressSegment.SegmentText.Submitted -> stringResource(R.string.claim_status_detail_submitted)
    ClaimProgressSegment.SegmentText.BeingHandled -> stringResource(R.string.claim_status_bar_being_handled)
    ClaimProgressSegment.SegmentText.Closed -> stringResource(R.string.claim_status_detail_closed)
  }
  ClaimProgress(
    text = text,
    color = color,
    textColor = textColor,
    modifier = modifier,
  )
}

@Composable
private fun ClaimProgress(text: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(CircleShape)
        .background(color),
    )
    Spacer(modifier = Modifier.height(6.dp))
    HedvigText(
      text = text,
      textAlign = TextAlign.Center,
      style = HedvigTheme.typography.label,
      color = textColor,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimProgressRow() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimProgressRow(
        listOf(
          ClaimProgressSegment(
            Submitted,
            UNKNOWN,
          ),
          ClaimProgressSegment(
            BeingHandled,
            ACTIVE,
          ),
          ClaimProgressSegment(
            Closed,
            INACTIVE,
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      for (segmentType in SegmentType.entries) {
        ClaimProgress(
          segmentText = Closed,
          type = segmentType,
        )
      }
    }
  }
}
