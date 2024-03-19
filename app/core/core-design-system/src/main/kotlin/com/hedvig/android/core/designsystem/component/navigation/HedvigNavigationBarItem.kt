package com.hedvig.android.core.designsystem.component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import com.hedvig.android.core.designsystem.component.tokens.value
import com.hedvig.android.core.designsystem.hedvig.MappedInteractionSource
import com.hedvig.android.core.designsystem.hedvig.ProvideContentColorTextStyle
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3ColorScheme
import com.hedvig.android.core.designsystem.material3.LocalHedvigMaterial3ColorScheme
import com.hedvig.android.core.designsystem.material3.fromToken
import com.hedvig.android.core.designsystem.material3.toTextStyle
import kotlin.math.roundToInt

/**
 * @param modifier the [Modifier] to be applied to this navigation bar
 * @param containerColor the color used for the background of this navigation bar. Use
 * [Color.Transparent] to have no color.
 * @param contentColor the preferred color for content inside this navigation bar. Defaults to
 * either the matching content color for [containerColor], or to the current [LocalContentColor] if
 * [containerColor] is not a color from the theme.
 * @param tonalElevation when [containerColor] is [ColorScheme.surface], a translucent primary color
 * overlay is applied on top of the container. A higher tonal elevation value will result in a
 * darker color in light theme and lighter color in dark theme. See also: [Surface].
 * @param windowInsets a window insets of the navigation bar.
 * @param content the content of this navigation bar, typically 3-5 [NavigationBarItem]s
 */
@Composable
fun HedvigNavigationBar(
  modifier: Modifier = Modifier,
  containerColor: Color = NavigationBarDefaults.containerColor,
  contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
  tonalElevation: Dp = NavigationBarDefaults.Elevation,
  windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
  content: @Composable RowScope.() -> Unit,
) {
  Surface(
    color = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(windowInsets)
        .defaultMinSize(minHeight = NavigationBarHeight)
        .selectableGroup(),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}

@Composable
fun RowScope.HedvigNavigationBarItem(
  selected: Boolean,
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  label: String,
  colors: HedvigNavigationBarItemColors = HedvigNavigationBarItemDefaults.colors(),
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val styledIcon = @Composable {
    val iconColor by colors.iconColor(selected = selected, enabled = enabled)
    Box(modifier = Modifier.clearAndSetSemantics {}) {
      CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
    }
  }

  val styledLabel: @Composable () -> Unit = {
    val style = HedvigNavigationBarTokens.LabelTextFont.toTextStyle().copy()
    val textColor by colors.textColor(selected = selected, enabled = enabled)
    ProvideContentColorTextStyle(
      contentColor = textColor,
      textStyle = style,
      content = {
        val density = LocalDensity.current
        val fontCappedDensity = Density(
          density = density.density,
          fontScale = density.fontScale.coerceAtMost(NavigationBarFontScaleCap),
        )
        CompositionLocalProvider(LocalDensity provides fontCappedDensity) {
          Text(
            text = label,
            overflow = TextOverflow.Clip,
            softWrap = false,
            maxLines = 1,
          )
        }
      },
    )
  }

  var itemWidth by remember { mutableIntStateOf(0) }

  Box(
    modifier
      .selectable(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        role = Role.Tab,
        interactionSource = interactionSource,
        indication = null,
      )
      .defaultMinSize(minHeight = NavigationBarHeight)
      .weight(1f)
      .onSizeChanged {
        itemWidth = it.width
      },
    contentAlignment = Alignment.Center,
    propagateMinConstraints = true,
  ) {
    val animationProgress: State<Float> = animateFloatAsState(
      targetValue = if (selected) 1f else 0f,
      animationSpec = tween(ItemAnimationDurationMillis),
    )

    // The entire item is selectable, but only the indicator pill shows the ripple. To achieve
    // this, we re-map the coordinates of the item's InteractionSource into the coordinates of
    // the indicator.
    val deltaOffset: Offset
    with(LocalDensity.current) {
      val indicatorWidth = HedvigNavigationBarTokens.ActiveIndicatorWidth.roundToPx()
      deltaOffset = Offset(
        (itemWidth - indicatorWidth).toFloat() / 2,
        IndicatorVerticalOffset.toPx(),
      )
    }
    val offsetInteractionSource = remember(interactionSource, deltaOffset) {
      MappedInteractionSource(interactionSource, deltaOffset)
    }

    // The indicator has a width-expansion animation which interferes with the timing of the
    // ripple, which is why they are separate composables
    val indicatorRipple = @Composable {
      Box(
        Modifier
          .layoutId(IndicatorRippleLayoutIdTag)
          .clip(HedvigNavigationBarTokens.ActiveIndicatorShape.value)
          .indication(
            offsetInteractionSource,
            rememberRipple(),
          ),
      )
    }
    val indicator = @Composable {
      Box(
        Modifier
          .layoutId(IndicatorLayoutIdTag)
          .graphicsLayer { alpha = animationProgress.value }
          .background(
            color = colors.indicatorColor,
            shape = HedvigNavigationBarTokens.ActiveIndicatorShape.value,
          ),
      )
    }

    NavigationBarItemLayout(
      indicatorRipple = indicatorRipple,
      indicator = indicator,
      icon = styledIcon,
      label = styledLabel,
      animationProgress = { animationProgress.value },
    )
  }
}

object HedvigNavigationBarItemDefaults {
  /**
   * Creates a [NavigationBarItemColors] with the provided colors according to the Material
   * specification.
   */
  @Composable
  fun colors() = LocalHedvigMaterial3ColorScheme.current.defaultNavigationBarItemColors

  /**
   * Creates a [NavigationBarItemColors] with the provided colors according to the Material
   * specification.
   *
   * @param selectedIconColor the color to use for the icon when the item is selected.
   * @param selectedTextColor the color to use for the text label when the item is selected.
   * @param indicatorColor the color to use for the indicator when the item is selected.
   * @param unselectedIconColor the color to use for the icon when the item is unselected.
   * @param unselectedTextColor the color to use for the text label when the item is unselected.
   * @param disabledIconColor the color to use for the icon when the item is disabled.
   * @param disabledTextColor the color to use for the text label when the item is disabled.
   * @return the resulting [NavigationBarItemColors] used for [NavigationBarItem]
   */
  @Composable
  fun colors(
    selectedIconColor: Color = Color.Unspecified,
    selectedTextColor: Color = Color.Unspecified,
    indicatorColor: Color = Color.Unspecified,
    unselectedIconColor: Color = Color.Unspecified,
    unselectedTextColor: Color = Color.Unspecified,
    disabledIconColor: Color = Color.Unspecified,
    disabledTextColor: Color = Color.Unspecified,
  ): HedvigNavigationBarItemColors = LocalHedvigMaterial3ColorScheme.current.defaultNavigationBarItemColors.copy(
    selectedIconColor = selectedIconColor,
    selectedTextColor = selectedTextColor,
    selectedIndicatorColor = indicatorColor,
    unselectedIconColor = unselectedIconColor,
    unselectedTextColor = unselectedTextColor,
    disabledIconColor = disabledIconColor,
    disabledTextColor = disabledTextColor,
  )

  internal val HedvigMaterial3ColorScheme.defaultNavigationBarItemColors: HedvigNavigationBarItemColors
    get() {
      return defaultNavigationBarItemColorsCached ?: HedvigNavigationBarItemColors(
        selectedIconColor = fromToken(HedvigNavigationBarTokens.ActiveIconColor),
        selectedTextColor = fromToken(HedvigNavigationBarTokens.ActiveLabelTextColor),
        selectedIndicatorColor = fromToken(HedvigNavigationBarTokens.ActiveIndicatorColor),
        unselectedIconColor = fromToken(HedvigNavigationBarTokens.InactiveIconColor),
        unselectedTextColor = fromToken(HedvigNavigationBarTokens.InactiveLabelTextColor),
        disabledIconColor = fromToken(HedvigNavigationBarTokens.InactiveIconColor).copy(alpha = DisabledAlpha),
        disabledTextColor = fromToken(HedvigNavigationBarTokens.InactiveLabelTextColor).copy(alpha = DisabledAlpha),
      ).also {
        defaultNavigationBarItemColorsCached = it
      }
    }
}

@Stable
class HedvigNavigationBarItemColors(
  val selectedIconColor: Color,
  val selectedTextColor: Color,
  val selectedIndicatorColor: Color,
  val unselectedIconColor: Color,
  val unselectedTextColor: Color,
  val disabledIconColor: Color,
  val disabledTextColor: Color,
) {
  /**
   * Returns a copy of this NavigationBarItemColors, optionally overriding some of the values.
   * This uses the Color.Unspecified to mean “use the value from the source”
   */
  fun copy(
    selectedIconColor: Color = this.selectedIconColor,
    selectedTextColor: Color = this.selectedTextColor,
    selectedIndicatorColor: Color = this.selectedIndicatorColor,
    unselectedIconColor: Color = this.unselectedIconColor,
    unselectedTextColor: Color = this.unselectedTextColor,
    disabledIconColor: Color = this.disabledIconColor,
    disabledTextColor: Color = this.disabledTextColor,
  ) = HedvigNavigationBarItemColors(
    selectedIconColor.takeOrElse { this.selectedIconColor },
    selectedTextColor.takeOrElse { this.selectedTextColor },
    selectedIndicatorColor.takeOrElse { this.selectedIndicatorColor },
    unselectedIconColor.takeOrElse { this.unselectedIconColor },
    unselectedTextColor.takeOrElse { this.unselectedTextColor },
    disabledIconColor.takeOrElse { this.disabledIconColor },
    disabledTextColor.takeOrElse { this.disabledTextColor },
  )

  /**
   * Represents the icon color for this item, depending on whether it is [selected].
   *
   * @param selected whether the item is selected
   * @param enabled whether the item is enabled
   */
  @Composable
  internal fun iconColor(selected: Boolean, enabled: Boolean): State<Color> {
    val targetValue = when {
      !enabled -> disabledIconColor
      selected -> selectedIconColor
      else -> unselectedIconColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(ItemAnimationDurationMillis),
    )
  }

  /**
   * Represents the text color for this item, depending on whether it is [selected].
   *
   * @param selected whether the item is selected
   * @param enabled whether the item is enabled
   */
  @Composable
  internal fun textColor(selected: Boolean, enabled: Boolean): State<Color> {
    val targetValue = when {
      !enabled -> disabledTextColor
      selected -> selectedTextColor
      else -> unselectedTextColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(ItemAnimationDurationMillis),
    )
  }

  /** Represents the color of the indicator used for selected items. */
  internal val indicatorColor: Color
    get() = selectedIndicatorColor

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is NavigationBarItemColors) return false

    if (selectedIconColor != other.selectedIconColor) return false
    if (unselectedIconColor != other.unselectedIconColor) return false
    if (selectedTextColor != other.selectedTextColor) return false
    if (unselectedTextColor != other.unselectedTextColor) return false
    if (selectedIndicatorColor != other.selectedIndicatorColor) return false
    if (disabledIconColor != other.disabledIconColor) return false
    if (disabledTextColor != other.disabledTextColor) return false

    return true
  }

  override fun hashCode(): Int {
    var result = selectedIconColor.hashCode()
    result = 31 * result + unselectedIconColor.hashCode()
    result = 31 * result + selectedTextColor.hashCode()
    result = 31 * result + unselectedTextColor.hashCode()
    result = 31 * result + selectedIndicatorColor.hashCode()
    result = 31 * result + disabledIconColor.hashCode()
    result = 31 * result + disabledTextColor.hashCode()

    return result
  }
}

/**
 * Base layout for a [NavigationBarItem].
 *
 * @param indicatorRipple indicator ripple for this item when it is selected
 * @param indicator indicator for this item when it is selected
 * @param icon icon for this item
 * @param label text label for this item
 * @param alwaysShowLabel whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 * @param animationProgress progress of the animation, where 0 represents the unselected state of
 * this item and 1 represents the selected state. This value controls other values such as indicator
 * size, icon and label positions, etc.
 */
@Composable
private fun NavigationBarItemLayout(
  indicatorRipple: @Composable () -> Unit,
  indicator: @Composable () -> Unit,
  icon: @Composable () -> Unit,
  label: @Composable () -> Unit,
  animationProgress: () -> Float,
) {
  Layout(
    {
      indicatorRipple()
      indicator()
      Box(Modifier.layoutId(IconLayoutIdTag)) { icon() }
      Box(Modifier.layoutId(LabelLayoutIdTag)) { label() }
    },
  ) { measurables, constraints ->
    @Suppress("NAME_SHADOWING")
    val animationProgress = animationProgress()
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val iconPlaceable = measurables.fastFirst { it.layoutId == IconLayoutIdTag }.measure(looseConstraints)

    val totalIndicatorWidth = iconPlaceable.width + (IndicatorHorizontalPadding * 2).roundToPx()
    val animatedIndicatorWidth = (totalIndicatorWidth * animationProgress).roundToInt()
    val indicatorHeight = iconPlaceable.height + (IndicatorVerticalPadding * 2).roundToPx()
    val indicatorRipplePlaceable = measurables
      .fastFirst { it.layoutId == IndicatorRippleLayoutIdTag }
      .measure(
        Constraints.fixed(
          width = totalIndicatorWidth,
          height = indicatorHeight,
        ),
      )
    val indicatorPlaceable = measurables
      .fastFirstOrNull { it.layoutId == IndicatorLayoutIdTag }
      ?.measure(
        Constraints.fixed(
          width = animatedIndicatorWidth,
          height = indicatorHeight,
        ),
      )

    val labelPlaceable = measurables
      .fastFirst { it.layoutId == LabelLayoutIdTag }
      .measure(
        looseConstraints.copy(
          maxWidth = constraints.maxWidth + NavigationBarLabelHorizontalBreakoutPadding.roundToPx(),
        ),
      )

    placeLabelAndIcon(
      labelPlaceable,
      iconPlaceable,
      indicatorRipplePlaceable,
      indicatorPlaceable,
      constraints,
      animationProgress,
    )
  }
}

/**
 * Places the provided [Placeable]s in the correct position, depending on [alwaysShowLabel] and
 * [animationProgress].
 *
 * When [alwaysShowLabel] is true, the positions do not move. The [iconPlaceable] and
 * [labelPlaceable] will be placed together in the center with padding between them, according to
 * the spec.
 *
 * When [animationProgress] is 1 (representing the selected state), the positions will be the same
 * as above.
 *
 * Otherwise, when [animationProgress] is 0, [iconPlaceable] will be placed in the center, like in
 * [placeIcon], and [labelPlaceable] will not be shown.
 *
 * When [animationProgress] is animating between these values, [iconPlaceable] and [labelPlaceable]
 * will be placed at a corresponding interpolated position.
 *
 * [indicatorRipplePlaceable] and [indicatorPlaceable] will always be placed in such a way that to
 * share the same center as [iconPlaceable].
 *
 * @param labelPlaceable text label placeable inside this item
 * @param iconPlaceable icon placeable inside this item
 * @param indicatorRipplePlaceable indicator ripple placeable inside this item
 * @param indicatorPlaceable indicator placeable inside this item, if it exists
 * @param constraints constraints of the item
 * @param animationProgress progress of the animation, where 0 represents the unselected state of
 * this item and 1 represents the selected state. Values between 0 and 1 interpolate positions of
 * the icon and label.
 */
private fun MeasureScope.placeLabelAndIcon(
  labelPlaceable: Placeable,
  iconPlaceable: Placeable,
  indicatorRipplePlaceable: Placeable,
  indicatorPlaceable: Placeable?,
  constraints: Constraints,
  animationProgress: Float,
): MeasureResult {
  val contentHeight = iconPlaceable.height + IndicatorVerticalPadding.toPx() +
    NavigationBarIndicatorToLabelPadding.toPx() + labelPlaceable.height
  val contentVerticalPadding = ((constraints.minHeight - contentHeight) / 2)
    .coerceAtLeast(IndicatorVerticalPadding.toPx())
  val height = contentHeight + contentVerticalPadding * 2

  // Icon (when selected) should be `contentVerticalPadding` from top
  val selectedIconY = contentVerticalPadding
  val unselectedIconY = selectedIconY

  // How far the icon needs to move between unselected and selected states.
  val iconDistance = unselectedIconY - selectedIconY

  // The interpolated fraction of iconDistance that all placeables need to move based on
  // animationProgress.
  val offset = iconDistance * (1 - animationProgress)

  // Label should be fixed padding below icon
  val labelY = selectedIconY + iconPlaceable.height + IndicatorVerticalPadding.toPx() +
    NavigationBarIndicatorToLabelPadding.toPx()

  val containerWidth = constraints.maxWidth

  val labelX = (containerWidth - labelPlaceable.width) / 2
  val iconX = (containerWidth - iconPlaceable.width) / 2

  val rippleX = (containerWidth - indicatorRipplePlaceable.width) / 2
  val rippleY = selectedIconY - IndicatorVerticalPadding.toPx()

  return layout(containerWidth, height.roundToInt()) {
    indicatorPlaceable?.let {
      val indicatorX = (containerWidth - it.width) / 2
      val indicatorY = selectedIconY - IndicatorVerticalPadding.roundToPx()
      it.placeRelative(indicatorX, (indicatorY + offset).roundToInt())
    }
    labelPlaceable.placeRelative(labelX, (labelY + offset).roundToInt())
    iconPlaceable.placeRelative(iconX, (selectedIconY + offset).roundToInt())
    indicatorRipplePlaceable.placeRelative(rippleX, (rippleY + offset).roundToInt())
  }
}

private const val IndicatorRippleLayoutIdTag: String = "indicatorRipple"

private const val IndicatorLayoutIdTag: String = "indicator"

private const val IconLayoutIdTag: String = "icon"

private const val LabelLayoutIdTag: String = "label"

private val NavigationBarHeight: Dp = HedvigNavigationBarTokens.ContainerHeight

private const val ItemAnimationDurationMillis: Int = 100

private val NavigationBarIndicatorToLabelPadding: Dp = 4.dp

/**
 * The maximum font scale for the label text. This is to prevent the label from being too large and having to wrap or
 * truncate the text which is discouraged in the spec
 * https://m3.material.io/components/navigation-bar/guidelines#5a0e52ea-eb3f-42ef-96a4-160c7faf3c93
 */
private val NavigationBarFontScaleCap: Float = 1.3f

/**
 * The label is allowed to break out of its max constraints by this amount to give it more room before it needs to be
 * clipped. This is theoretically allowing the texts to overlap a bit, but in practice it's an okay tradeoff since only
 * one of our labels is in comparison much bigger than the others.
 */
private val NavigationBarLabelHorizontalBreakoutPadding = 8.dp

private val IndicatorHorizontalPadding: Dp =
  (HedvigNavigationBarTokens.ActiveIndicatorWidth - HedvigNavigationBarTokens.IconSize) / 2

private val IndicatorVerticalPadding: Dp =
  (HedvigNavigationBarTokens.ActiveIndicatorHeight - HedvigNavigationBarTokens.IconSize) / 2

private val IndicatorVerticalOffset: Dp = 12.dp
