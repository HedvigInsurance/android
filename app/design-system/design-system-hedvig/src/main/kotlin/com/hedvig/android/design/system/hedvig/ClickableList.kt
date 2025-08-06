package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ClickableListDefaults.iconSize
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeClickableListTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumClickableListTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallClickableListTokens

@Composable
fun ClickableList(
  items: List<ClickableItem>,
  onItemClick: (index: Int) -> Unit,
  size: ClickableListDefaults.Size,
  style: ClickableListDefaults.Style,
  modifier: Modifier = Modifier,
) {
  val verticalArrangement = when (style) {
    ClickableListDefaults.Style.Default -> Arrangement.Top
    ClickableListDefaults.Style.Filled -> Arrangement.spacedBy(4.dp)
  }
  Column(
    modifier = modifier,
    verticalArrangement = verticalArrangement,
  ) {
    items.forEachIndexed { index, item ->
      val containerColor = listColors.containerColor(style)
      ClickableListItem(
        item = item,
        containerColor = containerColor,
        onItemClick = {
          onItemClick(index)
        },
        size = size,
        style = style,
        modifier = Modifier.horizontalDivider(
          position = DividerPosition.Top,
          show = style is ClickableListDefaults.Style.Default && index != 0,
          color = listColors.dividerColor,
        ),
      )
    }
  }
}

@Composable
private fun ClickableListItem(
  item: ClickableItem,
  containerColor: Color,
  onItemClick: () -> Unit,
  style: ClickableListDefaults.Style,
  size: ClickableListDefaults.Size,
  modifier: Modifier = Modifier,
) {
  val shape = when (style) {
    ClickableListDefaults.Style.Default -> RectangleShape
    ClickableListDefaults.Style.Filled -> size.shape
  }
  Column(modifier) {
    Surface(
      shape = shape,
      color = containerColor,
      modifier = Modifier
        .clip(shape)
        .clickable(onClick = onItemClick),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.padding(size.padding(style)),
        startSlot = {
          HedvigText(
            text = item.text,
            style = size.textStyle,
          )
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(HedvigIcons.ChevronRight, "", Modifier.size(iconSize))
          }
        },
        spaceBetween = 4.dp,
      )
    }
  }
}

object ClickableListDefaults {
  internal val iconSize = 24.dp

  sealed class Size {
    @get:Composable
    internal abstract val shape: Shape
    protected abstract val defaultPadding: PaddingValues
    protected abstract val filledPadding: PaddingValues

    @get:Composable
    internal abstract val textStyle: TextStyle

    internal fun padding(style: Style): PaddingValues {
      return when (style) {
        Style.Default -> defaultPadding
        Style.Filled -> filledPadding
      }
    }

    data object Small : Size() {
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = SmallClickableListTokens.ContainerShape.value
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          start = SmallClickableListTokens.DefaultHorizontalPadding,
          end = SmallClickableListTokens.DefaultHorizontalPadding,
          top = SmallClickableListTokens.DefaultTopPadding,
          bottom = SmallClickableListTokens.DefaultBottomPadding,
        )
      override val filledPadding: PaddingValues
        get() = PaddingValues(
          start = SmallClickableListTokens.FilledHorizontalPadding,
          end = SmallClickableListTokens.FilledHorizontalPadding,
          top = SmallClickableListTokens.FilledTopPadding,
          bottom = SmallClickableListTokens.FilledBottomPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = SmallClickableListTokens.TextFont.value
    }

    data object Medium : Size() {
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MediumClickableListTokens.ContainerShape.value
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          start = MediumClickableListTokens.DefaultHorizontalPadding,
          end = MediumClickableListTokens.DefaultHorizontalPadding,
          top = MediumClickableListTokens.DefaultTopPadding,
          bottom = MediumClickableListTokens.DefaultBottomPadding,
        )
      override val filledPadding: PaddingValues
        get() = PaddingValues(
          start = MediumClickableListTokens.FilledHorizontalPadding,
          end = MediumClickableListTokens.FilledHorizontalPadding,
          top = MediumClickableListTokens.FilledTopPadding,
          bottom = MediumClickableListTokens.FilledBottomPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MediumClickableListTokens.TextFont.value
    }

    data object Large : Size() {
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LargeClickableListTokens.ContainerShape.value
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          start = LargeClickableListTokens.DefaultHorizontalPadding,
          end = LargeClickableListTokens.DefaultHorizontalPadding,
          top = LargeClickableListTokens.DefaultTopPadding,
          bottom = LargeClickableListTokens.DefaultBottomPadding,
        )
      override val filledPadding: PaddingValues
        get() = PaddingValues(
          start = LargeClickableListTokens.FilledHorizontalPadding,
          end = LargeClickableListTokens.FilledHorizontalPadding,
          top = LargeClickableListTokens.FilledTopPadding,
          bottom = LargeClickableListTokens.FilledBottomPadding,
        )
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LargeClickableListTokens.TextFont.value
    }
  }

  sealed class Style {
    data object Default : Style()

    data object Filled : Style()
  }
}

data class ClickableItem(
  val text: String,
)

private data class ClickableListColor(
  private val defaultContainerColor: Color,
  private val filledContainerColor: Color,
  val dividerColor: Color,
  val textColor: Color,
  val iconColor: Color,
) {
  fun containerColor(style: ClickableListDefaults.Style): Color {
    return when (style) {
      ClickableListDefaults.Style.Default -> defaultContainerColor
      ClickableListDefaults.Style.Filled -> filledContainerColor
    }
  }
}

private val listColors: ClickableListColor
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      ClickableListColor(
        defaultContainerColor = fromToken(ColorSchemeKeyTokens.Transparent),
        filledContainerColor = fromToken(ColorSchemeKeyTokens.SurfacePrimary),
        dividerColor = fromToken(ColorSchemeKeyTokens.BorderPrimary),
        textColor = fromToken(ColorSchemeKeyTokens.TextPrimary),
        iconColor = fromToken(ColorSchemeKeyTokens.FillPrimary),
      )
    }
  }
