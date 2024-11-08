package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Saver
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.shared.tier.comparison.data.ComparisonData
import com.hedvig.android.shared.tier.comparison.data.ComparisonRow
import com.hedvig.android.shared.tier.comparison.data.mockComparisonData
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success
import hedvig.resources.R
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
      ProvideTextStyle(HedvigTheme.typography.label) {
        ComparisonScreen(state, navigateUp)
      }
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
  val scrollState = rememberScrollState()
  IndicateScrollableTableEffect(scrollState)

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

    Box {
      var rippleOffset by rememberSaveable(stateSaver = IntOffset.Saver) {
        mutableStateOf(IntOffset(0, 0))
      }
      val interactionSource = remember { MutableInteractionSource() }
      Table(
        uiState = uiState,
        selectComparisonRow = { comparisonRow ->
          bottomSheetRow = comparisonRow
        },
        onSetRippleOffset = { intOffset ->
          rippleOffset = intOffset
        },
        interactionSource = interactionSource,
        scrollState = scrollState,
      )
      Box(
        modifier = Modifier
          .offset {
            rippleOffset
          }
          .indication(
            interactionSource = interactionSource,
            indication = ripple(),
          ),
        sizeAdjustingContent = {
          HedvigText(
            "ripple",
            fontSize = HedvigTheme.typography.label.fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .padding(vertical = 8.dp)
              .fillMaxWidth()
              .withoutPlacement(),
          )
        },
      ) {
      }
    }
  }
}

/**
 * Used when first entering the screen to give a hint to the user that the table is scrollable, because it's a bit
 *  subtle otherwise
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
private fun Table(
  uiState: Success,
  selectComparisonRow: (ComparisonRow) -> Unit,
  onSetRippleOffset: (IntOffset) -> Unit,
  interactionSource: MutableInteractionSource,
  scrollState: ScrollState,
) {
  val density = LocalDensity.current
  var tableConstraints: Constraints? by remember { mutableStateOf(null) }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .layout { measurable, constraints ->
        tableConstraints = constraints
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
          placeable.placeRelative(0, 0)
        }
      },
  ) {
    FixSizedComparisonDataColumn(
      comparisonData = uiState.comparisonData,
      selectComparisonRow = selectComparisonRow,
      interactionSource = interactionSource,
      onSetRippleOffset = onSetRippleOffset,
      modifier = Modifier
        .then(
          if (tableConstraints != null) {
            Modifier.widthIn(
              max = with(density) {
                (tableConstraints!!.maxWidth * 0.4f).toDp()
              },
            )
          } else {
            Modifier
          },
        )
        .width(IntrinsicSize.Max),
    )
    val borderColor = HedvigTheme.colorScheme.borderSecondary
    val shadowWidth by remember { derivedStateOf { if (scrollState.value > 0) 4.dp else 0.dp } }
    val animatedShadowSize by animateDpAsState(shadowWidth)
    ScrollableTableSection(
      scrollState = scrollState,
      comparisonData = uiState.comparisonData,
      selectedColumnIndex = uiState.selectedColumnIndex,
      onRowClick = selectComparisonRow,
      interactionSource = interactionSource,
      onSetRippleOffset = onSetRippleOffset,
      modifier = Modifier
        .weight(1f)
        .drawWithContent {
          drawContent()
          drawLine(
            color = borderColor,
            start = Offset.Zero,
            end = Offset(0f, size.height),
          )
          drawRect(
            brush = Brush.horizontalGradient(
              colors = listOf(borderColor, Color.Transparent),
              endX = animatedShadowSize.toPx(),
            ),
          )
        },
    )
  }
}

@Composable
private fun FixSizedComparisonDataColumn(
  modifier: Modifier = Modifier,
  comparisonData: ComparisonData,
  selectComparisonRow: (ComparisonRow) -> Unit,
  onSetRippleOffset: (IntOffset) -> Unit,
  interactionSource: MutableInteractionSource,
) {
  Column(modifier = modifier) {
    HedvigText(
      "H",
      modifier = Modifier
        .padding(vertical = 4.dp)
        .withoutPlacement(),
    )
    comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
      var rowOffset by rememberSaveable(stateSaver = IntOffset.Saver) {
        mutableStateOf(
          IntOffset(0, 0),
        )
      }
      val borderColor = HedvigTheme.colorScheme.borderSecondary
      Surface(
        color = HedvigTheme.colorScheme.backgroundPrimary,
        modifier = Modifier
          .fillMaxWidth()
          .onPlaced { layoutCoordinates ->
            rowOffset = layoutCoordinates
              .positionInParent()
              .round()
          }
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = { offset ->
                val press = PressInteraction.Press(offset)
                onSetRippleOffset(rowOffset)
                interactionSource.tryEmit(press)
                interactionSource.tryEmit(PressInteraction.Release(press))
              },
              onTap = { offset ->
                val press = PressInteraction.Press(offset)
                onSetRippleOffset(rowOffset)
                interactionSource.tryEmit(press)
                selectComparisonRow(comparisonRow)
                interactionSource.tryEmit(PressInteraction.Release(press))
              },
            )
          },
      ) {
        HedvigText(
          text = comparisonRow.title,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .padding(start = 16.dp)
            .drawWithContent {
              drawContent()
              if (rowIndex != 0) {
                drawLine(
                  color = borderColor,
                  start = Offset.Zero,
                  end = Offset(size.width, 0f),
                )
              }
            }
            .padding(end = 8.dp)
            .padding(vertical = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun ScrollableTableSection(
  scrollState: ScrollState,
  comparisonData: ComparisonData,
  selectedColumnIndex: Int?,
  onRowClick: (ComparisonRow) -> Unit,
  onSetRippleOffset: (IntOffset) -> Unit,
  interactionSource: MutableInteractionSource,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier.horizontalScroll(state = scrollState),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      for (column in comparisonData.columns) {
        val isThisSelected = comparisonData.columns.indexOf(column) == selectedColumnIndex
        val cellModifier = if (isThisSelected) {
          Modifier.background(
            shape = HedvigTheme.shapes.cornerXSmallTop,
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
            textAlign = TextAlign.Center,
            color = textColor,
            modifier = cellModifier
              .defaultMinSize(minWidth = 100.dp)
              .padding(vertical = 4.dp),
          )
        }
      }
    }
    comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
      var rowOffset by rememberSaveable(stateSaver = IntOffset.Saver) {
        mutableStateOf(
          IntOffset(0, 0),
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .onPlaced { layoutCoordinates ->
            rowOffset = layoutCoordinates
              .positionInParent()
              .round()
          }
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = { offset ->
                val press = PressInteraction.Press(offset)
                onSetRippleOffset(rowOffset)
                interactionSource.tryEmit(press)
                interactionSource.tryEmit(PressInteraction.Release(press))
              },
              onTap = { offset ->
                val press = PressInteraction.Press(offset)
                onSetRippleOffset(rowOffset)
                interactionSource.tryEmit(press)
                onRowClick(comparisonRow)
                interactionSource.tryEmit(PressInteraction.Release(press))
              },
            )
          },
      ) {
        comparisonRow.cells.forEachIndexed { index, cell ->
          val isThisSelected = index == selectedColumnIndex
          val cellModifier = if (isThisSelected) {
            Modifier.background(
              shape = if (rowIndex ==
                comparisonData.rows.lastIndex
              ) {
                HedvigTheme.shapes.cornerXSmallBottom
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
            drawTopBorder = rowIndex != 0,
          )
        }
        Spacer(Modifier.width(16.dp))
      }
    }
  }
}

@Composable
private fun CheckMarkCell(isCovered: Boolean, tint: Color, drawTopBorder: Boolean, modifier: Modifier = Modifier) {
  val borderColor = HedvigTheme.colorScheme.borderSecondary
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .defaultMinSize(minWidth = 100.dp)
      .drawWithContent {
        drawContent()
        if (drawTopBorder) {
          drawLine(
            color = borderColor,
            start = Offset.Zero,
            end = Offset(size.width, 0f),
          )
        }
      }
      .padding(vertical = 8.dp),
  ) {
    HedvigText(
      text = "H",
      modifier = Modifier.withoutPlacement(),
    )
    Icon(
      imageVector = if (isCovered) {
        HedvigIcons.Checkmark
      } else {
        HedvigIcons.Minus
      },
      null,
      tint = tint,
    )
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun ComparisonScreenPreview() {
  HedvigTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ComparisonScreen(
        Success(
          mockComparisonData,
          1,
        ),
        {},
      )
    }
  }
}
