package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTabletLandscapePreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalMinimumInteractiveComponentSize
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TableStyle
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.internal.MappedInteractionSource
import com.hedvig.android.shared.tier.comparison.data.ComparisonCell
import com.hedvig.android.shared.tier.comparison.data.ComparisonData
import com.hedvig.android.shared.tier.comparison.data.ComparisonRow
import com.hedvig.android.shared.tier.comparison.data.mockComparisonData
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success
import hedvig.resources.R
import kotlin.math.max
import kotlinx.coroutines.delay

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
  var bottomSheetRow by remember { mutableStateOf<ComparisonRow?>(null) }
  HedvigBottomSheet(
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
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
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
    Table(
      uiState = uiState,
      tableStyle = TableStyle(),
      selectComparisonRow = { comparisonRow ->
        bottomSheetRow = comparisonRow
      },
    )
  }
}

/**
 * The "fake" indication which is laid out on *top* of the table, so that when clicking on any part of the table, it
 * feels like the entire "row" is interacted with, despite those two items being completely separate composables.
 */
@Composable
private fun OverlaidIndication(
  textMeasurer: TextMeasurer,
  state: OverlaidIndicationState,
  modifier: Modifier = Modifier,
) {
  val topHeightOffset = with(LocalDensity.current) {
    calculateCellHeight(textMeasurer, forceMinimumInteractiveComponentSize = false).roundToPx()
  }
  val cellheight = calculateCellHeight(textMeasurer)
  Box(
    modifier = modifier
      .height(cellheight)
      .offset { IntOffset(0, topHeightOffset) + state.offset }
      .indication(
        interactionSource = state.interactionSource,
        indication = LocalIndication.current,
      ),
  )
}

/**
 * Used when first entering the screen to give a hint to the user that the table is scrollable, because it's a bit
 * subtle otherwise
 */
@Composable
private fun IndicateScrollableTableEffect(scrollState: ScrollState) {
  val density = LocalDensity.current
  LaunchedEffect(scrollState, density) {
    with(density) {
      delay(200)
      var absoluteXPosition = scrollState.value.toFloat()
      var lastVelocity = 0f
      scrollState.scroll {
        animate(
          initialValue = 0f,
          targetValue = (scrollState.maxValue).toFloat(),
          animationSpec = spring(stiffness = 20f, visibilityThreshold = 10.dp.toPx()),
        ) { currentValue, velocity ->
          // Ignore the last emission where we reach the `visibilityThreshold` and we "snap" to the end, since we're
          //  going to animate back right after anyway, and we want to do that while retaining the velocity we
          //  previously had.
          if (velocity != 0f) {
            lastVelocity = velocity
            absoluteXPosition += scrollBy(currentValue - absoluteXPosition)
          }
        }
        animate(
          initialValue = scrollState.value.toFloat(),
          targetValue = 0f,
          initialVelocity = lastVelocity,
          animationSpec = spring(stiffness = 60f, visibilityThreshold = 2.dp.toPx()),
        ) { currentValue, _ ->
          absoluteXPosition += scrollBy(currentValue - absoluteXPosition)
        }
      }
    }
  }
}

@Composable
private fun measureTableMeasurementInfo(
  textMeasurer: TextMeasurer,
  density: Density,
  textStyle: TextStyle,
  uiState: Success,
): TableMeasurementInfo {
  val maxWidthRequiredForFixedColumnTexts = remember(textMeasurer, uiState.comparisonData.rows, textStyle) {
    uiState
      .comparisonData
      .rows
      .map { it.title }
      .filterNotNull()
      .maxOf { title ->
        with(density) {
          textMeasurer.measure(
            text = title,
            style = textStyle,
            softWrap = false,
            maxLines = 1,
          ).size.width + ColumnTextStartPadding.roundToPx() + ColumnTextEndPadding.roundToPx()
        }
      }
  }
  val fixedCellWidth: Dp = remember(textMeasurer, uiState.comparisonData.columns, textStyle) {
    with(density) {
      uiState.comparisonData.columns.map { it.title }.filterNotNull().maxOf { title ->
        textMeasurer.measure(
          text = title,
          style = textStyle,
          softWrap = false,
          maxLines = 1,
        ).size.width
      }.toDp() + CellTitleHorizontalPadding
    }
  }
  val numberOfCells = remember(uiState.comparisonData.columns) {
    uiState.comparisonData.columns.mapNotNull { it.title }.count()
  }
  return TableMeasurementInfo(maxWidthRequiredForFixedColumnTexts, fixedCellWidth, numberOfCells)
}

data class TableMeasurementInfo(
  val maxWidthRequiredForFixedColumnTexts: Int,
  val fixedCellWidth: Dp,
  val numberOfCells: Int,
)

@Composable
private fun Table(
  uiState: Success,
  tableStyle: TableStyle,
  selectComparisonRow: (ComparisonRow) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textStyle = tableStyle.textStyle
  val density = LocalDensity.current
  val textMeasurer = rememberTextMeasurer(
    cacheSize = uiState.comparisonData.columns.count() + uiState.comparisonData.rows.count() + 1,
  )
  val overlaidIndicationState = rememberOverlaidIndicationState()
  val cellsScrollState = rememberScrollState()
  val tableMeasurementInfo = measureTableMeasurementInfo(textMeasurer, density, textStyle, uiState)
  ProvideTextStyle(textStyle) {
    Layout(
      {
        FixSizedComparisonDataColumn(
          tableStyle = tableStyle,
          textMeasurer = textMeasurer,
          comparisonData = uiState.comparisonData,
          selectComparisonRow = selectComparisonRow,
          cellsScrollState = cellsScrollState,
          overlaidIndicationState = overlaidIndicationState,
          modifier = Modifier.width(IntrinsicSize.Max),
        )
        ScrollableTableSection(
          tableStyle = tableStyle,
          textMeasurer = textMeasurer,
          fixedCellWidth = tableMeasurementInfo.fixedCellWidth,
          scrollState = cellsScrollState,
          comparisonData = uiState.comparisonData,
          selectedColumnIndex = uiState.selectedColumnIndex,
          onRowClick = selectComparisonRow,
          overlaidIndicationState = overlaidIndicationState,
        )
        OverlaidIndication(
          textMeasurer = textMeasurer,
          state = overlaidIndicationState,
        )
      },
      modifier = modifier.background(tableStyle.containerColor),
    ) { measurables, constraints ->
      val fixSizedComparisonDataColumn = measurables[0]
      val scrollableTableSection = measurables[1]
      val overlaidIndication = measurables[2]

      val cellSectionFullWidth =
        (tableMeasurementInfo.fixedCellWidth * tableMeasurementInfo.numberOfCells + CellStartPadding + CellEndPadding)
          .roundToPx()

      val bothFitCompletely =
        tableMeasurementInfo.maxWidthRequiredForFixedColumnTexts + cellSectionFullWidth <= constraints.maxWidth
      val columnConstraints = if (bothFitCompletely) {
        constraints.copy(
          minWidth = tableMeasurementInfo.maxWidthRequiredForFixedColumnTexts,
          maxWidth = tableMeasurementInfo.maxWidthRequiredForFixedColumnTexts,
        )
      } else {
        val spaceLeftAfterCellsAreFullyPlaced = constraints.maxWidth - cellSectionFullWidth
        val minimumSpaceFixedColumnShouldTakeIfBothDontFullyFit = constraints.maxWidth * MaxSizeRatioForFixedColumn
        val remainingWidthCoercedToAtLeastTheMinimumScreenPercentage =
          spaceLeftAfterCellsAreFullyPlaced.coerceAtLeast(minimumSpaceFixedColumnShouldTakeIfBothDontFullyFit.toInt())
        constraints.copy(
          minWidth = remainingWidthCoercedToAtLeastTheMinimumScreenPercentage,
          maxWidth = remainingWidthCoercedToAtLeastTheMinimumScreenPercentage,
        )
      }
      overlaidIndicationState.fixedColumnWidth = columnConstraints.maxWidth
      val fixSizedComparisonDataColumnPlaceable = fixSizedComparisonDataColumn.measure(columnConstraints)
      val spaceRemainingForCells = constraints.maxWidth - fixSizedComparisonDataColumnPlaceable.width
      val remainingWidthForCellSection = cellSectionFullWidth.coerceAtMost(spaceRemainingForCells)
      val cellSectionConstraints = constraints.copy(
        minWidth = remainingWidthForCellSection,
        maxWidth = remainingWidthForCellSection,
      )
      val scrollableTableSectionPlaceable = scrollableTableSection.measure(cellSectionConstraints)

      val layoutWidth = (fixSizedComparisonDataColumnPlaceable.width + scrollableTableSectionPlaceable.width)
        .coerceAtMost(constraints.maxWidth)
      val layoutHeight = max(fixSizedComparisonDataColumnPlaceable.height, scrollableTableSectionPlaceable.height)
      val overlaidIndicationPlaceable = overlaidIndication.measure(Constraints.fixedWidth(layoutWidth))
      layout(layoutWidth, layoutHeight) {
        scrollableTableSectionPlaceable.place(fixSizedComparisonDataColumnPlaceable.width, 0)
        fixSizedComparisonDataColumnPlaceable.place(0, 0)
        overlaidIndicationPlaceable.place(0, 0)
      }
    }
  }
}

@Composable
private fun FixSizedComparisonDataColumn(
  tableStyle: TableStyle,
  textMeasurer: TextMeasurer,
  comparisonData: ComparisonData,
  selectComparisonRow: (ComparisonRow) -> Unit,
  cellsScrollState: ScrollState,
  overlaidIndicationState: OverlaidIndicationState,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val shadowWidth by remember {
    with(density) {
      derivedStateOf {
        (1.dp + cellsScrollState.value.toDp() / 4).coerceAtMost(4.dp)
      }
    }
  }
  val animatedShadowSize by animateDpAsState(shadowWidth)
  Column(modifier = modifier) {
    Cell(textMeasurer, forceMinimumInteractiveComponentSize = false)
    Column(
      Modifier
        .drawWithContent {
          drawContent()
          drawLine(
            color = tableStyle.borderColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
          )
          val shadowSizePx = animatedShadowSize.toPx()
          drawRect(
            topLeft = Offset(size.width, 0f),
            size = Size(shadowSizePx, size.height),
            brush = Brush.horizontalGradient(
              colors = listOf(tableStyle.shadowColor, Color.Transparent),
              startX = size.width,
              endX = size.width + shadowSizePx,
            ),
          )
        },
    ) {
      comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
        val interactionSource = remember { MutableInteractionSource() }
        Cell(
          textMeasurer = textMeasurer,
          modifier = Modifier
            .fillMaxWidth()
            .overlaidIndicationConnection(overlaidIndicationState, interactionSource)
            .clickable(
              interactionSource = interactionSource,
              indication = null,
              onClick = { selectComparisonRow(comparisonRow) },
            ),
        ) {
          HedvigText(
            text = comparisonRow.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .padding(start = ColumnTextStartPadding)
              .drawWithContent {
                drawContent()
                if (rowIndex != 0) {
                  drawLine(
                    color = tableStyle.borderColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                  )
                }
              }
              .padding(end = ColumnTextEndPadding)
              .wrapContentSize(Alignment.CenterStart),
          )
        }
      }
    }
  }
}

@Composable
private fun ScrollableTableSection(
  tableStyle: TableStyle,
  textMeasurer: TextMeasurer,
  fixedCellWidth: Dp,
  scrollState: ScrollState,
  comparisonData: ComparisonData,
  selectedColumnIndex: Int?,
  onRowClick: (ComparisonRow) -> Unit,
  overlaidIndicationState: OverlaidIndicationState,
  modifier: Modifier = Modifier,
) {
  IndicateScrollableTableEffect(scrollState)
  Column(
    modifier.horizontalScroll(state = scrollState),
  ) {
    Row {
      Spacer(Modifier.width(CellStartPadding))
      comparisonData.columns.forEachIndexed { index, column ->
        column.title?.let { title ->
          val isThisSelected = index == selectedColumnIndex
          val textColor = tableStyle.cellTextColor(isThisSelected)
          Cell(
            textMeasurer = textMeasurer,
            fixedWidth = fixedCellWidth,
            forceMinimumInteractiveComponentSize = false,
            modifier = Modifier.background(
              shape = tableStyle.selectedCellShape(isFirst = true),
              color = tableStyle.cellContainerColor(isThisSelected),
            ),
          ) {
            HedvigText(
              text = title,
              textAlign = TextAlign.Center,
              color = textColor,
              modifier = Modifier.wrapContentHeight(),
            )
          }
        }
      }
    }
    Column {
      comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
        val mappedInteractionSource = remember {
          MappedInteractionSource(
            MutableInteractionSource(),
            { Offset(overlaidIndicationState.fixedColumnWidth.toFloat(), 0f) },
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .overlaidIndicationConnection(overlaidIndicationState, mappedInteractionSource)
            .clickable(
              interactionSource = mappedInteractionSource,
              indication = null,
              onClick = { onRowClick(comparisonRow) },
            )
            .drawWithContent {
              drawContent()
              if (rowIndex != 0) {
                drawLine(
                  color = tableStyle.borderColor,
                  start = Offset.Zero,
                  end = Offset(size.width, 0f),
                )
              }
            },
        ) {
          Spacer(Modifier.width(CellStartPadding))
          comparisonRow.cells.forEachIndexed { index, cell ->
            val isThisSelected = index == selectedColumnIndex
            ComparisonCell(
              tableStyle = tableStyle,
              textMeasurer = textMeasurer,
              cell = cell,
              isThisSelected = isThisSelected,
              fixedCellWidth = fixedCellWidth,
              modifier = Modifier.background(
                shape = tableStyle.selectedCellShape(isLast = rowIndex == comparisonData.rows.lastIndex),
                color = tableStyle.cellContainerColor(isThisSelected),
              ),
            )
          }
          Spacer(Modifier.width(CellEndPadding))
        }
      }
    }
  }
}

@Composable
private fun ComparisonCell(
  tableStyle: TableStyle,
  textMeasurer: TextMeasurer,
  cell: ComparisonCell,
  isThisSelected: Boolean,
  fixedCellWidth: Dp,
  modifier: Modifier = Modifier,
) {
  Cell(
    textMeasurer = textMeasurer,
    fixedWidth = fixedCellWidth,
    modifier = modifier,
  ) {
    val contentColor = tableStyle.cellContentColor(cell.isCovered, isThisSelected)
    Icon(
      imageVector = if (cell.isCovered) {
        HedvigIcons.Checkmark
      } else {
        HedvigIcons.Minus
      },
      null,
      tint = contentColor,
      modifier = Modifier
        .requiredSize(0.dp)
        .wrapContentSize(unbounded = true),
    )
  }
}

@Composable
private fun Cell(
  textMeasurer: TextMeasurer,
  fixedWidth: Dp? = null,
  modifier: Modifier = Modifier,
  forceMinimumInteractiveComponentSize: Boolean = true,
  content: @Composable () -> Unit = {},
) {
  val fixedHeight = calculateCellHeight(textMeasurer, forceMinimumInteractiveComponentSize)
  Box(
    modifier = modifier
      .then(
        if (fixedWidth != null) {
          Modifier.width(fixedWidth)
        } else {
          Modifier
        },
      )
      .requiredHeight(fixedHeight),
    propagateMinConstraints = true,
  ) {
    content()
  }
}

@Composable
private fun calculateCellHeight(textMeasurer: TextMeasurer, forceMinimumInteractiveComponentSize: Boolean = true): Dp {
  val textStyle = LocalTextStyle.current
  val density = LocalDensity.current
  val minimumInteractiveComponentSize = LocalMinimumInteractiveComponentSize.current
  return remember(textMeasurer) {
    val measuredHeight = with(density) {
      textMeasurer.measure(
        text = ConstantLetterUsedAsMeasurementPlaceholder,
        style = textStyle,
        softWrap = false,
        maxLines = 1,
      ).size.height.toDp() + (CellVerticalPadding * 2)
    }
    if (forceMinimumInteractiveComponentSize) {
      measuredHeight.coerceAtLeast(minimumInteractiveComponentSize)
    } else {
      measuredHeight
    }
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

private val ConstantLetterUsedAsMeasurementPlaceholder = "H"
private val CellStartPadding = 8.dp
private val CellEndPadding = 16.dp
private val CellVerticalPadding = 8.dp
private val CellTitleHorizontalPadding = 16.dp
private val ColumnTextStartPadding = 16.dp
private val ColumnTextEndPadding = 6.dp
private val MaxSizeRatioForFixedColumn = 0.4f
