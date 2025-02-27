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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

interface HedvigTabRowState {
  val selectedTabIndex: Int

  fun onTabChosen(index: Int)
}

@Composable
fun rememberHedvigTabRowState(): HedvigTabRowState {
  return rememberSaveable(
    saver = Saver(
      save = { it.selectedTabIndex },
      restore = { StandaloneHedvigTabRowState().apply { selectedTabIndex = it } },
    ),
  ) {
    StandaloneHedvigTabRowState()
  }
}

@Composable
fun rememberHedvigTabRowState(indexProvider: () -> Int, onTabChosenDelegate: (Int) -> Unit): HedvigTabRowState {
  return remember(indexProvider, onTabChosenDelegate) {
    DelegatedHedvigTabRowState(indexProvider, onTabChosenDelegate)
  }
}

private class StandaloneHedvigTabRowState() : HedvigTabRowState {
  override var selectedTabIndex: Int by mutableIntStateOf(0)
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

val IntOffset.Companion.Saver: Saver<IntOffset, Any>
  get() = listSaver(
    save = { listOf(it.x, it.y) },
    restore = { IntOffset(it[0], it[1]) },
  )
