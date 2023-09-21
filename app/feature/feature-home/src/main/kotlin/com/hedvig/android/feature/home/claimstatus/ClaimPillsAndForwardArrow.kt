package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.ui.Pill
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claimstatus.data.PillUiState

@Composable
internal fun ClaimPillsAndForwardArrow(
  pillsUiState: List<PillUiState>,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    pillsUiState.forEach { pillUiState: PillUiState ->
      ClaimPill(
        text = pillUiState.text,
        pillType = pillUiState.type,
      )
    }
  }
}

@Composable
private fun ClaimPill(
  text: String,
  pillType: PillUiState.PillType,
) {
  when (pillType) {
    PillUiState.PillType.OPEN -> Pill(text, MaterialTheme.colorScheme.outlineVariant)
    PillUiState.PillType.CLOSED -> Pill(text, MaterialTheme.colorScheme.primary)
    PillUiState.PillType.REOPENED -> Pill(
      text,
      MaterialTheme.colorScheme.warningContainer,
      MaterialTheme.colorScheme.onWarningContainer,
    )
    PillUiState.PillType.PAYMENT -> Pill(
      text,
      MaterialTheme.colorScheme.infoContainer,
      MaterialTheme.colorScheme.onInfoContainer,
    )
    PillUiState.PillType.UNKNOWN -> Pill(text, MaterialTheme.colorScheme.surface)
  }
}

@HedvigPreview
@Composable
private fun PreviewPills(
  @PreviewParameter(PillsUiStateProvider::class) pillsUiState: List<PillUiState>,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimPillsAndForwardArrow(pillsUiState)
    }
  }
}

private class PillsUiStateProvider : CollectionPreviewParameterProvider<List<PillUiState>>(
  listOf(
    PillUiState.previewList(),
    listOf(PillUiState.PillType.CLOSED, PillUiState.PillType.PAYMENT).map { pillType ->
      PillUiState(pillType.name, pillType)
    },
    listOf(PillUiState.PillType.REOPENED, PillUiState.PillType.OPEN).map { pillType ->
      PillUiState(pillType.name, pillType)
    },
    List(10) { PillUiState.PillType.REOPENED }.map { pillType ->
      PillUiState(pillType.name, pillType)
    },
  ),
)
