package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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

@Composable
fun HedvigTabRowMaxSixTabs(
  tabTitles: List<String>,
  selectedTabIndex: Int,
  onTabChosen: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  tabSize: TabSize = TabDefaults.defaultSize,
  tabStyle: TabStyle = TabDefaults.defaultStyle,
) {
  val currentWidthMap = remember { mutableStateMapOf<Int, Int>() }
  val currentHeightMap = remember { mutableStateMapOf<Int, Int>() }
  val currentOffsetMap = remember { mutableStateMapOf<Int, IntOffset>() }
  val indicatorOffset: IntOffset by animateIntOffsetAsState(
    targetValue = calculateIndicatorOffset(currentOffsetMap, selectedTabIndex),
    animationSpec = tween(
      durationMillis = 600,
      easing = FastOutSlowInEasing,
    ),
    label = "",
  )
  val density = LocalDensity.current
  val indicatorWidth: Dp by if (currentWidthMap.isEmpty()) remember { mutableStateOf(0.dp) } else animateDpAsState(
    targetValue = calculateIndicatorDimension(currentWidthMap, selectedTabIndex, density),
    animationSpec = tween(
      durationMillis = 600,
      easing = FastOutSlowInEasing,
    ),
  )
  val indicatorHeight: Dp by if (currentHeightMap.isEmpty()) remember { mutableStateOf(0.dp) } else animateDpAsState(
    targetValue = calculateIndicatorDimension(currentHeightMap, selectedTabIndex, density),
    animationSpec = tween(
      durationMillis = 600,
      easing = FastOutSlowInEasing,
    ),
  )
  var oneLineHeight by remember { mutableIntStateOf(0) }
  Box(
    modifier = modifier
        .clip(tabSize.rowShape)
        .background(tabStyle.colors.tabRowBackground)
        .height(intrinsicSize = IntrinsicSize.Min)
        .padding(tabSize.rowPadding),
  ) {
    Box(Modifier.withoutPlacement()) {
      HedvigText(
        text = "measurement", style = tabSize.textStyle,
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
            indicatorOffset = indicatorOffset,
            indicatorColor = tabStyle.colors.chosenTabBackground,
            indicatorShape = tabSize.tabShape,
          )
        },
        {
          tabTitles.forEachIndexed { index, title ->
            TabItem(
              modifier = Modifier
                .clip(tabSize.tabShape),
              onClick = {
                onTabChosen(index)
              },
              text = title,
              textStyle = tabSize.textStyle,
              tabTextColor = tabStyle.colors.textColor,
              contentPadding = tabSize.tabPadding,
            )
          }
        },
      ),
    ) { measurablesList: List<List<Measurable>>, constraints ->
      val itemMeasurables = measurablesList[1]
      if (itemMeasurables.size <= 1) {
        layout(0, 0) {}
      } else {
        val fullWidth = constraints.maxWidth
        val listOfPlaceables = mutableListOf<Placeable>()
        val desiredItemWidth =
          if (itemMeasurables.any { it.minIntrinsicWidth(oneLineHeight) > fullWidth / 3 }) fullWidth / 2
          else fullWidth / 3
        val howManyLines = itemMeasurables.size * desiredItemWidth / fullWidth
        val howManyItemsInEachLine = fullWidth / desiredItemWidth
        val mapOfOffsets = mutableMapOf<Int, IntOffset>()
        itemMeasurables.forEachIndexed { index, _ ->
          if (index<=howManyItemsInEachLine-1) { //first line
            mapOfOffsets[index] = IntOffset( desiredItemWidth * index, 0)
          } else if (index<=(2*howManyItemsInEachLine-1)) { //second line
            mapOfOffsets[index] = IntOffset(
              y = oneLineHeight,
              x = desiredItemWidth * (index - howManyItemsInEachLine)
            )
          } else { //third line
            mapOfOffsets[index] = IntOffset(
              y = oneLineHeight*2,
              x = desiredItemWidth * (index - howManyItemsInEachLine)
            )
          }
        }
        for (item in itemMeasurables) {
          val desiredItemHeight = oneLineHeight
          val placeable = item.measure(
            constraints.copy(
              maxWidth = desiredItemWidth,
              minWidth = desiredItemWidth,
              maxHeight = desiredItemHeight,
              minHeight = desiredItemHeight,
            ),
          )
          listOfPlaceables.add(placeable)
        }
        val chosenItem = listOfPlaceables[selectedTabIndex]
        val indicatorPlaceable = measurablesList[0][0].measure(
          Constraints(
            minWidth = chosenItem.width,
            minHeight = chosenItem.height,
            maxHeight = chosenItem.height,
            maxWidth = chosenItem.width,
          ),
        )
        layout(
          width = fullWidth,
          height = if (listOfPlaceables.size <= 3) oneLineHeight else oneLineHeight * 2,
        ) {
          indicatorPlaceable.placeRelative(mapOfOffsets[selectedTabIndex]!!.x, mapOfOffsets[selectedTabIndex]!!.y)
          listOfPlaceables.forEachIndexed { index, placeable ->
            placeable.placeRelative(
              x = mapOfOffsets[index]!!.x,
              y = mapOfOffsets[index]!!.y,
            )
          }
        } //todo: only for up to 6 TabItems
      }
    }
//
//    if (indicatorHeight != 0.dp && indicatorWidth != 0.dp) {
//      TabIndicator(
//        indicatorHeight = indicatorHeight,
//        indicatorWidth = indicatorWidth,
//        indicatorOffset = indicatorOffset,
//        indicatorColor = tabStyle.colors.chosenTabBackground,
//        indicatorShape = tabSize.tabShape,
//      )
//    }
//    TabFlowRow(
//        rowShape = tabSize.rowShape,
//        tabTitles = tabTitles,
//        textStyle = tabSize.textStyle,
//        textColor = tabStyle.colors.textColor,
//        contentPadding = tabSize.tabPadding,
//        onTabChosen = onTabChosen,
//        onItemPlaced = { index: Int, offset: IntOffset ->
//            currentOffsetMap[index] = offset
//        },
//        onSizeChanged = { index: Int, size: IntSize ->
//            currentWidthMap[index] = size.width
//            currentHeightMap[index] = size.height
//        },
//        tabShape = tabSize.tabShape,
//    )
  }
}

private fun calculateOffsetYinLayout(index: Int, firstItemRowHeight: Int): Int {
  return if (index <= 2) {
    0
  } else firstItemRowHeight
}

private fun calculateOffsetXinLayout(index: Int, itemWidth: Int): Int {
  return if (index == 0 || index == 3) {
    0
  } else if (index == 1 || index == 4) {
    itemWidth
  } else {
    2 * itemWidth
  }
}


private fun calculateIndicatorDimension(map: SnapshotStateMap<Int, Int>, selectedTabIndex: Int, density: Density): Dp {
  return with(density) {
    map[selectedTabIndex]?.toDp() ?: 0.dp
  }
}

private fun calculateIndicatorOffset(map: SnapshotStateMap<Int, IntOffset>, selectedTabIndex: Int): IntOffset {
  val result = map[selectedTabIndex] ?: IntOffset(0, 0)
  return result
}

object TabDefaults {
  internal val defaultSize: TabSize = Mini
  internal val defaultStyle: TabStyle = Default

  sealed class TabSize {
    @get:Composable
    internal abstract val rowShape: Shape

    @get:Composable
    internal abstract val tabShape: Shape

    internal abstract val rowPadding: PaddingValues
    internal abstract val tabPadding: PaddingValues

    @get:Composable
    internal abstract val textStyle: TextStyle

    data object Mini : TabSize() {
      override val rowPadding: PaddingValues
        get() = PaddingValues(
          horizontal = MiniTabTokens.RowHorizontalPadding,
          vertical = MiniTabTokens.RowVerticalPadding,
        )
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
      override val rowPadding: PaddingValues
        get() = PaddingValues(
          horizontal = SmallTabTokens.RowHorizontalPadding,
          vertical = SmallTabTokens.RowVerticalPadding,
        )
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
      override val rowPadding: PaddingValues
        get() = PaddingValues(
          horizontal = MediumTabTokens.RowHorizontalPadding,
          vertical = MediumTabTokens.RowVerticalPadding,
        )
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
      override val rowPadding: PaddingValues
        get() = PaddingValues(
          horizontal = LargeTabTokens.RowHorizontalPadding,
          vertical = LargeTabTokens.RowVerticalPadding,
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TabFlowRow(
  rowShape: Shape,
  tabShape: Shape,
  tabTitles: List<String>,
  onItemPlaced: (index: Int, offset: IntOffset) -> Unit,
  onSizeChanged: (index: Int, size: IntSize) -> Unit,
  onTabChosen: (Int) -> Unit,
  textStyle: TextStyle,
  textColor: Color,
  contentPadding: PaddingValues,
) {
  FlowRow(
    horizontalArrangement = Arrangement.Start,
    verticalArrangement = Arrangement.Center,
    maxItemsInEachRow = MAX_ITEMS_IN_ROW,
    maxLines = MAX_LINES,
    overflow = FlowRowOverflow.Visible,
    modifier = Modifier
        .clip(rowShape)
        .fillMaxWidth(),
  ) {
    tabTitles.forEachIndexed { index, title ->
      TabItem(
        modifier = Modifier
            .clip(tabShape)
            .weight(1f)
            .onPlaced { coordinates ->
                val offsetX = coordinates.positionInParent().x.toInt()
                val offsetY = coordinates.positionInParent().y.toInt()
                onItemPlaced(index, IntOffset(x = offsetX, y = offsetY))
            }
            .onSizeChanged { size ->
                onSizeChanged(index, size)
            },
        onClick = {
          onTabChosen(index)
        },
        text = title,
        textStyle = textStyle,
        tabTextColor = textColor,
        contentPadding = contentPadding,
      )
    }
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
private fun TabIndicator(
//  indicatorHeight: Dp,
//  indicatorWidth: Dp,
  indicatorOffset: IntOffset,
  indicatorColor: Color,
  indicatorShape: Shape,
) {
  Box(
    modifier = Modifier
//        .width(indicatorWidth)
//        .height(indicatorHeight)
        .offset {
            indicatorOffset
        }
        .clip(
            shape = indicatorShape,
        )
        .background(
            color = indicatorColor,
        ),
  )
}

private const val MAX_ITEMS_IN_ROW = 3
private const val MAX_LINES = 1
