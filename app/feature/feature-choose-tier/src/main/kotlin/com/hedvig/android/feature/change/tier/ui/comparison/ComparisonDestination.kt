package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.feature.change.tier.data.ComparisonRow
import com.hedvig.android.feature.change.tier.data.mockComparisonData
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Failure
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Loading
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Success
import kotlinx.coroutines.delay
import hedvig.resources.R

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
  var bottomSheetRow by remember { mutableStateOf<ComparisonRow?>(null) }
  var clickedRowIndex by remember { mutableStateOf<Int?>(null) }
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
    HedvigBottomSheet(
      sheetPadding = PaddingValues(0.dp),
      isVisible = bottomSheetRow != null,
      onVisibleChange = { isVisible ->
        if (!isVisible) {
          bottomSheetRow = null
        }
      },
    ) {
      Column {
        bottomSheetRow?.let { HedvigText(text = it.title) }
        Spacer(Modifier.height(16.dp))
        bottomSheetRow?.let {
          HedvigText(
            text = it.description,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
        val exactNumbers = bottomSheetRow?.numbers
        if (exactNumbers != null) {
          Spacer(Modifier.height(16.dp))
          HedvigText(
            text = exactNumbers,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
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
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState) {
      delay(200)
      scrollState.animateScrollTo(50)
      scrollState.animateScrollTo(0)
    }
    val interactionSource = remember { MutableInteractionSource()}
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          LayoutWithoutPlacement(
            sizeAdjustingContent =
            {
              HedvigText(
                "emptyspace",
                fontSize = HedvigTheme.typography.label.fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp),
              )
            },
          ) { }
          uiState.comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxSize()
                .clickable {
                  bottomSheetRow = comparisonRow
                },
            ) {
              RowTitle(
                comparisonRow = comparisonRow,
              )
            }
            if (rowIndex != uiState.comparisonData.rows.lastIndex) {
              HorizontalDivider()
            }
          }
        }

        Column(
          modifier = Modifier
            .weight(1.5f)
            .horizontalScroll(state = scrollState),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            for (column in uiState.comparisonData.columns) {
              val isThisSelected = uiState.comparisonData.columns.indexOf(column) == uiState.selectedColumnIndex
              val cellModifier = if (isThisSelected) {
                Modifier.background(
                  shape = HedvigTheme.shapes.smallTopCorners,
                  color = HedvigTheme.colorScheme.highlightGreenFill1,
                )
              } else {
                Modifier
              }
              val textColor =
                if (isThisSelected) HedvigTheme.colorScheme.textBlack else HedvigTheme.colorScheme.textPrimary
              column.title?.let {
                HedvigText(
                  it,
                  fontSize = HedvigTheme.typography.label.fontSize,
                  textAlign = TextAlign.Center,
                  color = textColor,
                  modifier = cellModifier
                    .defaultMinSize(minWidth = 100.dp)
                    .padding(vertical = 4.dp),
                )
              }

              Spacer(Modifier.width(2.dp))
            }
          }
          uiState.comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable {
                  bottomSheetRow = comparisonRow
                },
            ) {
              comparisonRow.cells.forEachIndexed { index, cell ->
                val isThisSelected = index == uiState.selectedColumnIndex
                val cellModifier = if (isThisSelected) {
                  Modifier.background(
                    shape = if (rowIndex ==
                      uiState.comparisonData.rows.lastIndex
                    ) {
                      HedvigTheme.shapes.smallBottomCorners
                    } else {
                      RectangleShape
                    },
                    color = HedvigTheme.colorScheme.highlightGreenFill1,
                  )
                } else {
                  Modifier
                }
                val checkMarkColor = if (!cell.isCovered) {
                  HedvigTheme.colorScheme.textDisabled
                } else if (isThisSelected) {
                  HedvigTheme.colorScheme.textBlack
                } else {
                  HedvigTheme.colorScheme.textPrimary
                }
                CheckMarkCell(
                  isCovered = cell.isCovered,
                  modifier = cellModifier,
                  tint = checkMarkColor,
                )
                Spacer(Modifier.width(2.dp))
              }
            }
            if (rowIndex != uiState.comparisonData.rows.lastIndex) {
              HorizontalDivider(
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowTitle(comparisonRow: ComparisonRow, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    HedvigText(
      text = comparisonRow.title,
      fontSize = HedvigTheme.typography.label.fontSize,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp),
    )
  }
}

@Composable
private fun CheckMarkCell(isCovered: Boolean, tint: Color, modifier: Modifier = Modifier) {
  Row(
    horizontalArrangement = Arrangement.Center,
    modifier = modifier
      .defaultMinSize(minWidth = 100.dp),
  ) {
    Icon(
      imageVector = if (isCovered) {
        HedvigIcons.Checkmark
      } else {
        HedvigIcons.Minus
      },
      null,
      tint = tint,
      modifier = Modifier.padding(vertical = 8.dp),
    )
  }
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
          0,
        ),
        {},
      )
    }
  }
}
