package com.hedvig.android.feature.claimhistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.toNonEmptyListOrThrow
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.R
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ClaimHistoryDestination(
  claimHistoryViewModel: ClaimHistoryViewModel,
  navigateUp: () -> Unit,
  navigateToClaimDetails: (String) -> Unit,
) {
  val uiState by claimHistoryViewModel.uiState.collectAsStateWithLifecycle()
  ClaimHistoryScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToClaimDetails = navigateToClaimDetails,
    reload = { claimHistoryViewModel.emit(ClaimHistoryEvent.Reload) },
  )
}

@Composable
private fun ClaimHistoryScreen(
  uiState: ClaimHistoryUiState,
  navigateUp: () -> Unit,
  navigateToClaimDetails: (String) -> Unit,
  reload: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.profile_claim_history_title),
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      ClaimHistoryUiState.Loading -> HedvigCircularProgressIndicator(
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .wrapContentSize(Alignment.Center)
          .size(24.dp),
      )

      ClaimHistoryUiState.Error -> HedvigErrorSection(
        onButtonClick = reload,
        modifier = Modifier
          .weight(1f)
          .wrapContentHeight(Alignment.CenterVertically)
          .fillMaxWidth(),
      )

      ClaimHistoryUiState.NoHistory -> EmptyState(
        text = stringResource(R.string.claim_history_empty_state_title),
        description = stringResource(R.string.claim_history_empty_state_body),
        modifier = Modifier
          .weight(1f)
          .wrapContentHeight(Alignment.CenterVertically)
          .fillMaxWidth(),
      )

      is ClaimHistoryUiState.Content -> ClaimHistoryContent(uiState, navigateToClaimDetails)
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.ClaimHistoryContent(
  uiState: ClaimHistoryUiState.Content,
  navigateToClaimDetails: (String) -> Unit,
) {
  uiState.claims.forEachIndexed { index, claim ->
    ClaimHistoryItem(index, claim, navigateToClaimDetails)
  }
}

@Composable
private fun ClaimHistoryItem(index: Int, claim: ClaimHistory, navigateToClaimDetails: (String) -> Unit) {
  val hedvigDateTimeFormatter = rememberHedvigDateTimeFormatter()
  HorizontalItemsWithMaximumSpaceTaken(
    {
      Column {
        HedvigText(
          text = claim.claimType ?: stringResource(R.string.CHAT_CONVERSATION_CLAIM_TITLE),
          style = HedvigTheme.typography.bodySmall,
        )
        HedvigText(
          buildString {
            append(stringResource(R.string.claim_status_claim_details_submitted))
            append(" ")
            append(
              hedvigDateTimeFormatter.format(
                claim.submittedAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
              ),
            )
          },
          style = HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.textSecondary),
        )
      }
    },
    {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      ) {
        if (claim.outcome != null && claim.outcome != ClaimHistory.ClaimOutcome.UNKNOWN) {
          HighlightLabel(
            labelText = stringResource(
              when (claim.outcome) {
                ClaimHistory.ClaimOutcome.PAID -> R.string.claim_decision_paid
                ClaimHistory.ClaimOutcome.NOT_COMPENSATED -> R.string.claim_decision_not_compensated
                ClaimHistory.ClaimOutcome.NOT_COVERED -> R.string.claim_decision_not_covered
                ClaimHistory.ClaimOutcome.UNRESPONSIVE -> R.string.claim_decision_unresponsive
                ClaimHistory.ClaimOutcome.UNKNOWN -> error("impossible")
              },
            ),
            size = HighlightLabelDefaults.HighLightSize.Small,
            color = HighlightLabelDefaults.HighlightColor.Outline,
          )
        }
        Icon(
          HedvigIcons.ChevronRight,
          null,
          Modifier.size(24.dp),
        )
      }
    },
    spaceBetween = 8.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        onClick = dropUnlessResumed {
          navigateToClaimDetails(claim.id)
        },
      )
      .horizontalDivider(DividerPosition.Top, show = index != 0, horizontalPadding = 18.dp)
      .padding(horizontal = 18.dp, vertical = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewClaimHistoryScreen(
  @PreviewParameter(ClaimHistoryUiStateCollectionPreviewParameterProvider::class) uiState: ClaimHistoryUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimHistoryScreen(
        uiState = uiState,
        {},
        {},
        {},
      )
    }
  }
}

private class ClaimHistoryUiStateCollectionPreviewParameterProvider :
  CollectionPreviewParameterProvider<ClaimHistoryUiState>(
    listOf(
      ClaimHistoryUiState.Content(
        List(3) {
          ClaimHistory(
            id = it.toString(),
            claimType = "$it",
            outcome = ClaimHistory.ClaimOutcome.entries[it],
            submittedAt = Instant.fromEpochMilliseconds(100),
          )
        }.toNonEmptyListOrThrow(),
      ),
      ClaimHistoryUiState.NoHistory,
      ClaimHistoryUiState.Error,
      ClaimHistoryUiState.Loading,
    ),
  )
