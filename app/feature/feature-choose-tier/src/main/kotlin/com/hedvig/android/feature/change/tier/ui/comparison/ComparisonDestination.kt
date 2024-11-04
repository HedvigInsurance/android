package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.change.tier.data.ComparisonRow
import com.hedvig.android.feature.change.tier.data.mockComparisonData
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Failure
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Loading
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Success
import kotlinx.coroutines.delay

@Composable
internal fun ComparisonDestination(viewModel: ComparisonViewModel, navigateUp: () -> Unit) {
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

    is Success -> ComparisonScreen(state, navigateUp)
    Failure -> {
      Box(Modifier.fillMaxSize()) {
        HedvigErrorSection(
          onButtonClick = {
            viewModel.emit(ComparisonEvent.Reload)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun ComparisonScreen(uiState: Success, navigateUp: () -> Unit) {
  var bottomSheetText by rememberSaveable { mutableStateOf<String?>(null) }
  HedvigBottomSheet(
    isVisible = bottomSheetText != null,
    onVisibleChange = { isVisible ->
      if (!isVisible) {
        bottomSheetText = null
      }
    },
  ) {
    bottomSheetText?.let { HedvigText(text = it) }
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
      text = "Compare coverage levels", //todo: remove hardcoded
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = "Explore the differences between available plans", //todo: remove hardcoded
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState) {
      delay(500)
      scrollState.animateScrollTo(scrollState.maxValue)
      scrollState.animateScrollTo(0)
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(modifier = Modifier.weight(1f)) {}
        Row(
          modifier = Modifier
            .weight(1.5f)
            .horizontalScroll(scrollState),
        ) {
          for (column in uiState.comparisonData.columns) {
            column?.let {
              HedvigText(
                it,
                fontSize = HedvigTheme.typography.label.fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.defaultMinSize(minWidth = 100.dp))
            }
            Spacer(Modifier.width(2.dp))
          }
        }
      }
      uiState.comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable {
            bottomSheetText = comparisonRow.description
          }
        ) {
          RowTitle(
            modifier = Modifier.weight(1f),
            comparisonRow = comparisonRow,
          )
          Row(
            modifier = Modifier
              .weight(1.5f)
              .horizontalScroll(scrollState),
          ) {
            for (cell in comparisonRow.cells) {
              CheckMarkCell(cell.isCovered)
              Spacer(Modifier.width(2.dp))
            }
          }
        }
        if (rowIndex != uiState.comparisonData.rows.lastIndex) {
          HorizontalDivider()
        }
      }
    }
  }
}

@Composable
private fun RowTitle(
  comparisonRow: ComparisonRow,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .padding(vertical = 12.dp)
  ) {
    HedvigText(
      text = comparisonRow.title,
      fontSize = HedvigTheme.typography.label.fontSize,
    )
  }
}


@Composable
private fun CheckMarkCell(isCovered: Boolean) {
  Icon(
    imageVector = if (isCovered) HedvigIcons.Checkmark
    else HedvigIcons.Minus,
    null,
    modifier = Modifier
      .padding(vertical = 8.dp)
      .defaultMinSize(minWidth = 100.dp)
      .wrapContentWidth(),
  )
}


@HedvigPreview
@Composable
private fun ComparisonScreenPreview() {
  HedvigTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ComparisonScreen(
        ComparisonState.Success(
          mockComparisonData,
        ),
        {},
      )
    }
  }
}
