package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
import kotlin.math.ceil

@Composable
fun HedvigTabRowMaxSixTabs(
  tabTitles: List<String>,
  selectedTabIndex: Int,
  onTabChosen: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  tabSize: TabSize = TabDefaults.defaultSize,
  tabStyle: TabStyle = TabDefaults.defaultStyle,
) {
  var currentIndicatorOffset by remember { mutableStateOf(IntOffset(0, 0)) }
  val indicatorOffset: IntOffset by animateIntOffsetAsState(
    targetValue = currentIndicatorOffset,
    animationSpec = tween(
      durationMillis = 600,
      easing = FastOutSlowInEasing,
    ),
    label = "",
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
          if (itemMeasurables.any { it.minIntrinsicWidth(oneLineHeight) > fullWidth / 3 }) {
            fullWidth / 2
          } else if (itemMeasurables.size == 2) {
            fullWidth / 2
          } else {
            fullWidth / 3
          }
        val howManyLines = (ceil(itemMeasurables.size.toDouble() * desiredItemWidth / fullWidth)).toInt()
        val howManyItemsInEachLine = fullWidth / desiredItemWidth
        val mapOfOffsets = mutableMapOf<Int, IntOffset>()
        itemMeasurables.forEachIndexed { index, _ ->
          if (index <= howManyItemsInEachLine - 1) { // first line
            mapOfOffsets[index] = IntOffset(desiredItemWidth * index, 0)
          } else if (index <= (2 * howManyItemsInEachLine - 1)) { // second line
            mapOfOffsets[index] = IntOffset(
              y = oneLineHeight,
              x = desiredItemWidth * (index - howManyItemsInEachLine),
            )
          } else { // third line
            mapOfOffsets[index] = IntOffset(
              y = oneLineHeight * 2,
              x = desiredItemWidth * (index - howManyItemsInEachLine),
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
          height = howManyLines * oneLineHeight,
        ) {
          currentIndicatorOffset = IntOffset(mapOfOffsets[selectedTabIndex]!!.x, mapOfOffsets[selectedTabIndex]!!.y)
          indicatorPlaceable.placeRelative(indicatorOffset)
          listOfPlaceables.forEachIndexed { index, placeable ->
            placeable.placeRelative(
              x = mapOfOffsets[index]!!.x,
              y = mapOfOffsets[index]!!.y,
            )
          }
        } // only for up to 6 TabItems
      }
    }
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
private fun TabIndicator(indicatorColor: Color, indicatorShape: Shape) {
  Box(
    modifier = Modifier
      .clip(
        shape = indicatorShape,
      )
      .background(
        color = indicatorColor,
      ),
  )
}
