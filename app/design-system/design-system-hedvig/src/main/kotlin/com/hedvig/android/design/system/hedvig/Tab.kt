package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
fun HedvigTabRow(
  tabTitles: List<String>,
  selectedTabIndex: Int,
  onTabChosen: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  tabSize: TabSize = TabDefaults.defaultSize,
  tabStyle: TabStyle = TabDefaults.defaultStyle,
) {
  val density = LocalDensity.current
  val currentWidthMap = remember { mutableStateMapOf<Int, Int>() }
  val indicatorOffset: Dp by animateDpAsState(
    targetValue = calculateOffset(currentWidthMap, selectedTabIndex, density),
    animationSpec = tween(easing = LinearEasing),
  )
  val indicatorWidth: Dp by animateDpAsState(
    targetValue = calculateIndicatorWidth(currentWidthMap, selectedTabIndex, density),
    animationSpec = tween(easing = LinearEasing),
  )
  Box(
    modifier = modifier
      .clip(tabSize.rowShape)
      .background(tabStyle.colors.tabRowBackground)
      .height(intrinsicSize = IntrinsicSize.Min)
      .padding(tabSize.rowPadding),
  ) {
    TabIndicator(
      indicatorWidth = indicatorWidth,
      indicatorOffset = indicatorOffset,
      indicatorColor = tabStyle.colors.chosenTabBackground,
      indicatorShape = tabSize.tabShape,
    )
    Row(
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(tabSize.rowShape)
        .fillMaxWidth(),
    ) {
      tabTitles.mapIndexed { index, text ->
        TabItem(
          modifier = Modifier
            .clip(tabSize.tabShape)
            .weight(1f),
          onClick = {
            onTabChosen(index)
          },
          text = text,
          textStyle = tabSize.textStyle,
          tabTextColor = tabStyle.colors.textColor,
          contentPadding = tabSize.tabPadding,
          onTextMeasured = {
            currentWidthMap[index] = it
          },
        )
      }
    }
  }
}

private fun calculateOffset(map: SnapshotStateMap<Int, Int>, selectedTabIndex: Int, density: Density): Dp {
  var offset = 0
  for (i in 0..<selectedTabIndex) {
    offset += map[i] ?: 0
  }
  return with(density) {
    offset.toDp()
  }
}

private fun calculateIndicatorWidth(map: SnapshotStateMap<Int, Int>, selectedTabIndex: Int, density: Density): Dp {
  return with(density) {
    map[selectedTabIndex]?.toDp() ?: 5.dp
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
  onTextMeasured: (width: Int) -> Unit,
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
      .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        onTextMeasured(placeable.width)
        layout(placeable.width, placeable.height) {
          placeable.placeRelative(0, 0)
        }
      }
      .padding(contentPadding),
    text = text,
    style = textStyle,
    color = tabTextColor,
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun TabIndicator(indicatorWidth: Dp, indicatorOffset: Dp, indicatorColor: Color, indicatorShape: Shape) {
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(
        width = indicatorWidth,
      )
      .offset(
        x = indicatorOffset,
      )
      .clip(
        shape = indicatorShape,
      )
      .background(
        color = indicatorColor,
      ),
  )
}
