package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTabletLandscapePreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.shared.tier.comparison.data.ComparisonRow
import com.hedvig.android.shared.tier.comparison.data.mockComparisonData
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success
import hedvig.resources.R

@Composable
fun ComparisonDestination(viewModel: ComparisonViewModel, navigateUp: () -> Unit) {
  val uiState: ComparisonState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    Loading -> {
      Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        HedvigCircularProgressIndicator()
      }
    }

    is Success -> {
      ComparisonScreen(state, navigateUp)
    }

    Failure -> {
      Box(Modifier.fillMaxSize()) {
        HedvigErrorSection(
          onButtonClick = {
            viewModel.emit(Reload)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun ComparisonScreen(uiState: Success, navigateUp: () -> Unit) {
  val selectedComparisonRowBottomSheetState = rememberHedvigBottomSheetState<ComparisonRow>()
  HedvigBottomSheet(
    hedvigBottomSheetState = selectedComparisonRowBottomSheetState,
    contentPadding = PaddingValues(horizontal = 24.dp),
  ) { comparisonRow ->
    ComparisonRowBottomSheetContent(comparisonRow, { selectedComparisonRowBottomSheetState.dismiss() })
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { navigateUp() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.TIER_COMPARISON_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(R.string.TIER_COMPARISON_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    // todo add new comparison table content
  }
}

@Composable
private fun ComparisonRowBottomSheetContent(
  comparisonRow: ComparisonRow,
  dismissSheet: () -> Unit,
) {
  Column {
    HedvigText(text = comparisonRow.title)
    Spacer(Modifier.height(2.dp))
    HedvigText(
      text = comparisonRow.description,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    val exactNumbers = comparisonRow.numbers
    if (exactNumbers != null) { // todo see if we want to remove this part completely
      Spacer(Modifier.height(2.dp))
      HedvigText(
        text = exactNumbers,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      onClick = dismissSheet,
      text = stringResource(R.string.general_close_button),
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@HedvigPreview
@HedvigTabletLandscapePreview
@PreviewFontScale
@Preview
@Composable
private fun ComparisonScreenPreview(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withExtraData: Boolean,
) {
  val comparisonData = if (withExtraData) {
    val rows = mockComparisonData.rows.map {
      it.copy(
        title = it.title.repeat(2),
        cells = it.cells + it.cells,
      )
    }
    val columns = (mockComparisonData.columns + mockComparisonData.columns)
    mockComparisonData.copy(
      rows = rows + rows,
      columns = columns,
    )
  } else {
    mockComparisonData
  }
  HedvigTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ComparisonScreen(
        Success(
          comparisonData,
          1,
        ),
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewComparisonRowBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ComparisonRowBottomSheetContent(mockComparisonData.rows.first(), {})
    }
  }
}
