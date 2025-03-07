package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Mini
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Default
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.ButtonSecondaryAltResting
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.ButtonSecondaryResting
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfacePrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.LargeTabTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumTabTokens
import com.hedvig.android.design.system.hedvig.tokens.MiniTabTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallTabTokens
import com.hedvig.android.design.system.hedvig.tokens.TabTokens
import kotlin.math.ceil
import kotlinx.coroutines.launch

interface HedvigTabRowState {
  val selectedTabIndex: Int

  fun onTabChosen(index: Int)
}

@Composable
fun rememberHedvigTabRowState(initialIndex: Int = 0): HedvigTabRowState {
  return rememberSaveable(
    saver = Saver(
      save = { it.selectedTabIndex },
      restore = { StandaloneHedvigTabRowState(it) },
    ),
  ) {
    StandaloneHedvigTabRowState(initialIndex)
  }
}

@Composable
fun rememberHedvigTabRowState(pagerState: PagerState): HedvigTabRowState {
  val coroutineScope = rememberCoroutineScope()
  return remember(pagerState, coroutineScope) {
    DelegatedHedvigTabRowState(
      indexProvider = { pagerState.currentPage },
      onTabChosenDelegate = { coroutineScope.launch { pagerState.animateScrollToPage(it) } },
    )
  }
}

private class StandaloneHedvigTabRowState(initialIndex: Int = 0) : HedvigTabRowState {
  override var selectedTabIndex: Int by mutableIntStateOf(initialIndex)
    internal set

  override fun onTabChosen(index: Int) {
    selectedTabIndex = index
  }
}

private class DelegatedHedvigTabRowState(
  private val indexProvider: () -> Int,
  private val onTabChosenDelegate: (Int) -> Unit,
) : HedvigTabRowState {
  override val selectedTabIndex: Int get() = indexProvider()

  override fun onTabChosen(index: Int) {
    onTabChosenDelegate(index)
  }
}

@Composable
fun HedvigTabRow(
  tabTitles: List<String>,
  modifier: Modifier = Modifier,
  tabRowState: HedvigTabRowState = rememberHedvigTabRowState(),
  tabSize: TabSize = TabDefaults.defaultSize,
  tabStyle: TabStyle = TabDefaults.defaultStyle,
  selectIndicatorAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
    durationMillis = 600,
    easing = FastOutSlowInEasing,
  ),
) {
  val textStyle = tabSize.textStyle
  val tabInternalPadding = tabSize.tabPadding
  val textMeasurer = rememberTextMeasurer(tabTitles.size)
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current
  val (minItemWidth, fixedItemHeight) = remember(textMeasurer, tabTitles, density) {
    var width = 0
    var height = 0
    for (title in tabTitles) {
      val textLayoutResult = textMeasurer.measure(title, textStyle, maxLines = 1)
      width = maxOf(width, textLayoutResult.size.width)
      height = maxOf(height, textLayoutResult.size.height)
    }
    with(density) {
      val extraHorizontalSpace = tabInternalPadding.calculateStartPadding(layoutDirection)
        .plus(tabInternalPadding.calculateEndPadding(layoutDirection))
      val extraVerticalSpace = tabInternalPadding.calculateTopPadding() + tabInternalPadding.calculateBottomPadding()
      DpSize(width.toDp() + extraHorizontalSpace, height.toDp() + extraVerticalSpace)
    }
  }
  var indicatorOffset by rememberSaveable(stateSaver = IntOffset.Saver) { mutableStateOf(IntOffset(-1, -1)) }
  val animatedIndicatorOffset: IntOffset by if (indicatorOffset == IntOffset.Uninitialized) {
    remember { mutableStateOf(IntOffset.Uninitialized) }
  } else {
    animateIntOffsetAsState(
      targetValue = indicatorOffset,
      animationSpec = selectIndicatorAnimationSpec,
      label = "indicatorOffset",
    )
  }

  Box(
    modifier = modifier
      .clip(tabSize.rowShape)
      .background(tabStyle.colors.tabRowBackground)
      .padding(tabSize.rowPadding),
  ) {
    Layout(
      contents = listOf(
        {
          TabIndicator(
            indicatorColor = tabStyle.colors.chosenTabBackground,
            indicatorShape = tabSize.tabShape,
          )
        },
        {
          tabTitles.forEachIndexed { index, title ->
            TabItem(
              onClick = { tabRowState.onTabChosen(index) },
              text = title,
              textStyle = tabSize.textStyle,
              tabTextColor = tabStyle.colors.textColor,
              contentPadding = tabSize.tabPadding,
              modifier = Modifier.clip(tabSize.tabShape),
            )
          }
        },
      ),
    ) { measurables: List<List<Measurable>>, constraints ->
      val indicator = measurables[0][0]
      val tabMeasureables = measurables[1]

      val layoutInformation = TabRowLayoutInformation(
        density,
        constraints,
        tabRowState,
        minItemWidth,
        fixedItemHeight,
        tabTitles.size,
      )
      indicatorOffset = layoutInformation.calculateIndicatorOffset()

      val placeableIndicator = indicator.measure(layoutInformation.itemMeasurableConstraints)
      val placeableTabItems = tabMeasureables.map { measurableTab ->
        measurableTab.measure(layoutInformation.itemMeasurableConstraints)
      }
      layout(layoutInformation.layoutWidth, layoutInformation.layoutHeight) {
        if (animatedIndicatorOffset != IntOffset.Uninitialized) {
          placeableIndicator.placeRelative(animatedIndicatorOffset)
        }
        var y = 0
        placeableTabItems.chunked(layoutInformation.maxItemsPerRow) { tabItems ->
          var x = layoutInformation.calculateStartingXPositionForNumberOfTabItemsInRow(tabItems.size)
          for (tabItem in tabItems) {
            tabItem.placeRelative(x, y)
            x += tabItem.width
          }
          y += fixedItemHeight.roundToPx()
        }
      }
    }
  }
}

private data class TabRowLayoutInformation(
  private val layoutDensity: Density,
  private val constraints: Constraints,
  private val tabRowState: HedvigTabRowState,
  private val minItemWidth: Dp,
  private val fixedItemHeight: Dp,
  private val numberOfItems: Int,
) : Density by layoutDensity {
  val maxItemsPerRow = constraints.maxWidth / minItemWidth.roundToPx()
  val rowsRequired = (numberOfItems / maxItemsPerRow) + (if (numberOfItems % maxItemsPerRow == 0) 0 else 1)
  val realItemsPerRow: Int = if (rowsRequired == 1) {
    numberOfItems
  } else {
    maxItemsPerRow
  }
  val widthAllowedPerItem = constraints.maxWidth / realItemsPerRow

  val layoutWidth = constraints.maxWidth
  val layoutHeight = rowsRequired * fixedItemHeight.roundToPx()

  val itemMeasurableConstraints = Constraints.fixed(widthAllowedPerItem, fixedItemHeight.roundToPx())

  fun calculateIndicatorOffset(): IntOffset {
    val selectedIndex = tabRowState.selectedTabIndex
    val matchingRowIndex = selectedIndex / realItemsPerRow
    val indexInRow = selectedIndex % realItemsPerRow
    val startingX = calculateStartingXPositionForNumberOfTabItemsInRow(
      calculateNumberOfItemsInRowNumber(matchingRowIndex),
    )
    return IntOffset(
      startingX + indexInRow * widthAllowedPerItem,
      matchingRowIndex * fixedItemHeight.roundToPx(),
    )
  }

  fun calculateStartingXPositionForNumberOfTabItemsInRow(numberOfTabItems: Int): Int {
    val remainingWidth = layoutWidth - (numberOfTabItems * widthAllowedPerItem)
    return remainingWidth / 2
  }

  fun calculateNumberOfItemsInRowNumber(rowIndex: Int): Int {
    return when {
      rowsRequired == 1 -> numberOfItems
      rowIndex == rowsRequired - 1 -> numberOfItems % maxItemsPerRow
      else -> realItemsPerRow
    }
  }
}

private val IntOffset.Companion.Uninitialized: IntOffset
  get() = IntOffset(-1, -1)

// only for up to 6 TabItems!
@Composable
fun HedvigTabRowMaxSixTabs(
  tabTitles: List<String>,
  modifier: Modifier = Modifier,
  tabRowState: HedvigTabRowState = rememberHedvigTabRowState(),
  tabSize: TabSize = TabDefaults.defaultSize,
  tabStyle: TabStyle = TabDefaults.defaultStyle,
  selectIndicatorAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
    durationMillis = 600,
    easing = FastOutSlowInEasing,
  ),
) {
  var currentIndicatorOffset by rememberSaveable(stateSaver = IntOffset.Saver) { mutableStateOf(IntOffset(0, 0)) }
  val indicatorOffset: IntOffset by animateIntOffsetAsState(
    targetValue = currentIndicatorOffset,
    animationSpec = selectIndicatorAnimationSpec,
    label = "",
  )
  var oneLineHeight by remember { mutableIntStateOf(0) }
  Box(
    modifier = modifier
      .clip(tabSize.rowShape)
      .background(tabStyle.colors.tabRowBackground)
      .padding(tabSize.rowPadding),
  ) {
    Box(Modifier.withoutPlacement()) {
      // to get one line height
      HedvigText(
        text = "measurement",
        style = tabSize.textStyle,
        modifier = Modifier
          .onSizeChanged {
            oneLineHeight = it.height
          }
          .padding(tabSize.tabPadding),
      )
    }
    Layout(
      contents = listOf(
        {
          TabIndicator(
            indicatorColor = tabStyle.colors.chosenTabBackground,
            indicatorShape = tabSize.tabShape,
          )
        },
        {
          tabTitles.take(6).forEachIndexed { index, title ->
            TabItem(
              modifier = Modifier
                .clip(tabSize.tabShape),
              onClick = {
                tabRowState.onTabChosen(index)
              },
              text = title,
              textStyle = tabSize.textStyle,
              tabTextColor = tabStyle.colors.textColor,
              contentPadding = tabSize.tabPadding,
            )
          }
        },
      ),
    ) { allMeasurables: List<List<Measurable>>, constraints ->
      val tabItemsMeasurables = allMeasurables[1]

      // calculate the size
      val fullWidth = constraints.maxWidth
      val desiredItemWidth =
        if (tabItemsMeasurables.size <= 1) {
          fullWidth
        } else if (tabItemsMeasurables.size == 2) {
          fullWidth / 2
          // if there are only two tabs they always take full width
        } else if (tabItemsMeasurables.any { it.minIntrinsicWidth(oneLineHeight) > fullWidth / 3 }) {
          fullWidth / 2
          // this is the maximum width we give the item, if the text is too big for the basic 1/3 of the full width.
          // If it's still not enough, the text itself gets eclipsed later on
        } else {
          fullWidth / 3
          // basic
        }

      // calculate the offsets
      val howManyLines = (ceil(tabItemsMeasurables.size.toDouble() * desiredItemWidth / fullWidth)).toInt()
      val howManyItemsInEachLine = fullWidth / desiredItemWidth
      val fullLayoutCapacity = howManyLines * howManyItemsInEachLine
      val isLastRowFull = tabItemsMeasurables.size == fullLayoutCapacity
      val howManyItemsToCenter = if (isLastRowFull || tabItemsMeasurables.size == 2) {
        0
      } else {
        howManyItemsInEachLine - (fullLayoutCapacity - tabItemsMeasurables.size)
      }
      val indicesToCenter = tabItemsMeasurables.indices.filter {
        it > tabItemsMeasurables.lastIndex - howManyItemsToCenter
      }

      val mapOfOffsets = mutableMapOf<Int, IntOffset>()

      tabItemsMeasurables.forEachIndexed { index, _ ->
        val currentRow = if (index <= howManyItemsInEachLine - 1) {
          0
        } else if (index <= (2 * howManyItemsInEachLine - 1)) {
          1
        } else {
          2
        }

        val verticalOffset = oneLineHeight * currentRow

        val horizontalOffset = if (indicesToCenter.contains(index)) {
          // last line not full, centered
          calculateHorizontalOffsetForCentered(
            desiredItemWidth = desiredItemWidth,
            fullWidth = fullWidth,
            howManyItemsToCenter = howManyItemsToCenter,
            currentIndex = index,
            lastIndex = tabItemsMeasurables.lastIndex,
          )
        } else { // full line
          val adjustedIndex = index - (currentRow * howManyItemsInEachLine)
          desiredItemWidth * adjustedIndex
        }

        mapOfOffsets[index] = IntOffset(
          y = verticalOffset,
          x = horizontalOffset,
        )
      }

      // measure
      val tabItemsPlaceables = mutableListOf<Placeable>()
      for (item in tabItemsMeasurables) {
        val desiredItemHeight = oneLineHeight
        val placeable = item.measure(
          constraints.copy(
            maxWidth = desiredItemWidth,
            minWidth = desiredItemWidth,
            maxHeight = desiredItemHeight,
            minHeight = desiredItemHeight,
          ),
        )
        tabItemsPlaceables.add(placeable)
      }
      val chosenItem = tabItemsPlaceables[tabRowState.selectedTabIndex]
      val indicatorPlaceable = allMeasurables[0][0].measure(
        Constraints(
          minWidth = chosenItem.width,
          minHeight = chosenItem.height,
          maxHeight = chosenItem.height,
          maxWidth = chosenItem.width,
        ),
      )

      // placing first indicator, then items
      layout(
        width = fullWidth,
        height = howManyLines * oneLineHeight,
      ) {
        currentIndicatorOffset = IntOffset(
          mapOfOffsets[tabRowState.selectedTabIndex]!!.x,
          mapOfOffsets[tabRowState.selectedTabIndex]!!.y,
        )
        indicatorPlaceable.placeRelative(indicatorOffset)
        tabItemsPlaceables.forEachIndexed { index, placeable ->
          placeable.placeRelative(
            x = mapOfOffsets[index]!!.x,
            y = mapOfOffsets[index]!!.y,
          )
        }
      }
    }
  }
}

private fun calculateHorizontalOffsetForCentered(
  desiredItemWidth: Int,
  fullWidth: Int,
  howManyItemsToCenter: Int,
  currentIndex: Int,
  lastIndex: Int,
): Int {
  return if (howManyItemsToCenter == 1) {
    (fullWidth - desiredItemWidth) / 2
  } else if (currentIndex == lastIndex) {
    (desiredItemWidth * 1.5).toInt()
  } else {
    (desiredItemWidth * 0.5).toInt()
  }
}

object TabDefaults {
  internal val defaultSize: TabSize = Mini
  internal val defaultStyle: TabStyle = Default

  sealed class TabSize {
    @get:Composable
    internal abstract val rowShape: Shape

    @get:Composable
    internal abstract val tabShape: Shape

    internal val rowPadding: PaddingValues = PaddingValues(
      horizontal = TabTokens.RowHorizontalPadding,
      vertical = TabTokens.RowVerticalPadding,
    )
    internal abstract val tabPadding: PaddingValues

    @get:Composable
    internal abstract val textStyle: TextStyle

    data object Mini : TabSize() {
      override val tabPadding: PaddingValues
        get() = PaddingValues(
          horizontal = MiniTabTokens.TabHorizontalPadding,
          vertical = MiniTabTokens.TabVerticalPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MiniTabTokens.TextFont.value
      override val rowShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MiniTabTokens.ContainerShape.value
      override val tabShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MiniTabTokens.TabShape.value
    }

    data object Small : TabSize() {
      override val tabPadding: PaddingValues
        get() = PaddingValues(
          top = SmallTabTokens.TabTopPadding,
          bottom = SmallTabTokens.TabBottomPadding,
          start = SmallTabTokens.TabHorizontalPadding,
          end = SmallTabTokens.TabHorizontalPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = SmallTabTokens.TextFont.value
      override val rowShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = SmallTabTokens.ContainerShape.value
      override val tabShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = SmallTabTokens.TabShape.value
    }

    data object Medium : TabSize() {
      override val tabPadding: PaddingValues
        get() = PaddingValues(
          top = MediumTabTokens.TabTopPadding,
          bottom = MediumTabTokens.TabBottomPadding,
          start = MediumTabTokens.TabHorizontalPadding,
          end = MediumTabTokens.TabHorizontalPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MediumTabTokens.TextFont.value
      override val rowShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MediumTabTokens.ContainerShape.value
      override val tabShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MediumTabTokens.TabShape.value
    }

    data object Large : TabSize() {
      override val tabPadding: PaddingValues
        get() = PaddingValues(
          top = LargeTabTokens.TabTopPadding,
          bottom = LargeTabTokens.TabBottomPadding,
          start = LargeTabTokens.TabHorizontalPadding,
          end = LargeTabTokens.TabHorizontalPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LargeTabTokens.TextFont.value
      override val rowShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LargeTabTokens.ContainerShape.value
      override val tabShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LargeTabTokens.TabShape.value
    }
  }

  sealed class TabStyle {
    @get:Composable
    internal abstract val colors: TabColors

    data object Default : TabStyle() {
      override val colors: TabColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            TabColors(
              tabRowBackground = Color.Transparent,
              chosenTabBackground = fromToken(ButtonSecondaryResting),
              notChosenTabBackground = Color.Transparent,
              textColor = fromToken(TextPrimary),
            )
          }
        }
    }

    data object Filled : TabStyle() {
      override val colors: TabColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            TabColors(
              tabRowBackground = fromToken(SurfacePrimary),
              chosenTabBackground = fromToken(ButtonSecondaryAltResting),
              notChosenTabBackground = Color.Transparent,
              textColor = fromToken(TextPrimary),
            )
          }
        }
    }

    data class TabColors(
      val tabRowBackground: Color,
      val chosenTabBackground: Color,
      val notChosenTabBackground: Color,
      val textColor: Color,
    )
  }
}

@Composable
private fun TabItem(
  onClick: () -> Unit,
  text: String,
  textStyle: TextStyle,
  tabTextColor: Color,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  HedvigText(
    modifier = modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
      ) {
        onClick()
      }
      .padding(contentPadding),
    text = text,
    style = textStyle,
    overflow = TextOverflow.Ellipsis,
    color = tabTextColor,
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun TabIndicator(indicatorColor: Color, indicatorShape: Shape, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(shape = indicatorShape)
      .background(color = indicatorColor),
  )
}

val IntOffset.Companion.Saver: Saver<IntOffset, Any>
  get() = listSaver(
    save = { listOf(it.x, it.y) },
    restore = { IntOffset(it[0], it[1]) },
  )

@Preview(widthDp = 150)
@Preview(widthDp = 200)
@Preview(widthDp = 250)
@Preview(widthDp = 260)
@Preview(widthDp = 320)
@Preview(widthDp = 360)
@HedvigPreview
@Composable
private fun PreviewHedvigTabRow() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigTabRow(
        tabTitles = List(5) { "Title#$it" },
        tabRowState = rememberHedvigTabRowState(4),
        tabStyle = TabDefaults.TabStyle.Filled,
      )
    }
  }
}

@Preview(widthDp = 250)
@Composable
private fun PreviewHedvigTabRowWithVariousNumberOfItems(
  @PreviewParameter(NumberOfItemsProvider::class) numberOfItems: Int,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigTabRow(
        tabTitles = List(numberOfItems) { "Title#$it" },
        tabRowState = rememberHedvigTabRowState(4),
        tabStyle = TabDefaults.TabStyle.Filled,
      )
    }
  }
}

private class NumberOfItemsProvider : CollectionPreviewParameterProvider<Int>(List(10) { it + 1 })
