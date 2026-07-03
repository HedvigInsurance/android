package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.ui.claimstatus.internal.ClaimProgressRow
import com.hedvig.android.ui.claimstatus.model.ClaimCardUiState
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import hedvig.resources.RESUME_CLAIM_CONTINUE_BUTTON
import hedvig.resources.RESUME_CLAIM_DELETE_BUTTON
import hedvig.resources.RESUME_CLAIM_DRAFT
import hedvig.resources.RESUME_CLAIM_FALLBACK_TITLE
import hedvig.resources.RESUME_CLAIM_STATED
import hedvig.resources.Res
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun DraftClaimCard(
  uiState: ClaimCardUiState.Draft,
  onContinueClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier) {
    Column(Modifier.padding(16.dp)) {
      HighlightLabel(
        labelText = stringResource(Res.string.RESUME_CLAIM_DRAFT),
        size = HighLightSize.Small,
        color = HighlightColor.Amber(MEDIUM),
      )
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = uiState.title ?: stringResource(Res.string.RESUME_CLAIM_FALLBACK_TITLE),
        style = HedvigTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 2.dp),
      )
      val formattedDate = HedvigDateTimeFormatterDefaults
        .dateMonthAndYear(getLocale())
        .format(uiState.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()))
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_STATED, formattedDate),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 2.dp),
      )
      Spacer(Modifier.height(18.dp))
      ClaimProgressRow(
        claimProgressItemsUiState = listOf(
          ClaimProgressSegment(SegmentText.Started, INACTIVE),
          ClaimProgressSegment(SegmentText.BeingHandled, INACTIVE),
          ClaimProgressSegment(SegmentText.Closed, INACTIVE),
        ),
      )
      Spacer(Modifier.height(16.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigButton(
          text = stringResource(Res.string.RESUME_CLAIM_DELETE_BUTTON),
          onClick = onDeleteClick,
          enabled = true,
          buttonStyle = Secondary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
        HedvigButton(
          text = stringResource(Res.string.RESUME_CLAIM_CONTINUE_BUTTON),
          onClick = onContinueClick,
          enabled = true,
          buttonStyle = Primary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDraftClaimCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DraftClaimCard(
        uiState = ClaimCardUiState.Draft(
          id = "id",
          title = "My things",
          startedAt = Instant.parse("2026-07-02T00:00:00Z"),
        ),
        onContinueClick = {},
        onDeleteClick = {},
      )
    }
  }
}
