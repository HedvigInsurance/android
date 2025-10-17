package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens

data class RadioOption(
  val id: RadioOptionId,
  val text: String,
  val label: String? = null,
  val iconResource: IconResource? = null,
)

@JvmInline
value class RadioOptionId(val id: String)

enum class RadioGroupSize {
  Large, Medium, Small,
}

sealed interface RadioGroupStyle {
  data object Vertical : RadioGroupStyle
  data object LeftAligned : RadioGroupStyle
  data object Horizontal : RadioGroupStyle
  sealed interface Labeled : RadioGroupStyle {
    val label: String

    data class HorizontalFlow(override val label: String) : Labeled
    data class VerticalWithDivider(override val label: String) : Labeled
  }
}

@Composable
fun RadioGroup(
  options: List<RadioOption>,
  selectedOption: RadioOptionId?,
  onRadioOptionSelected: (RadioOptionId) -> Unit,
  size: RadioGroupSize = RadioGroupSize.Medium,
  style: RadioGroupStyle = RadioGroupStyle.Vertical,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val colors = RadioGroupDefaults.colors
  val spacings = RadioGroupDefaults.style(size, style)
  RadioGroup(
    options = options,
    onRadioOptionSelected = onRadioOptionSelected,
    selectedOptionId = selectedOption,
    colors = colors,
    style = spacings,
    enabled = enabled,
    modifier = modifier,
  )
}

private object RadioGroupDefaults {
  val colors: RadioGroupColors
    @Composable get() = RadioGroupColors(
      containerColor = RadioGroupColorTokens.ContainerColor.value,
      textColor = RadioGroupColorTokens.OptionTextColor.value,
      labelTextColor = RadioGroupColorTokens.LabelTextColor.value,
      disabledTextColor = RadioGroupColorTokens.DisabledOptionTextColor.value,
      disabledLabelTextColor = RadioGroupColorTokens.DisabledLabelTextColor.value,
      dividerColor = RadioGroupColorTokens.DividerColor.value,
      indicatorColor = RadioGroupColorTokens.IndicatorColor.value,
      indicatorSelectedColor = RadioGroupColorTokens.IndicatorSelectedColor.value,
      indicatorDisabledColor = RadioGroupColorTokens.IndicatorDisabledColor.value,
    )

  @Composable
  fun style(size: RadioGroupSize, style: RadioGroupStyle): RadioGroupStyleInternal {
    val tokens = when (size) {
      RadioGroupSize.Large -> RadioGroupSizeTokens.Large
      RadioGroupSize.Medium -> RadioGroupSizeTokens.Medium
      RadioGroupSize.Small -> RadioGroupSizeTokens.Small
    }
    return RadioGroupStyleInternal(
      style = style,
      topPadding = tokens.TopPadding,
      bottomPadding = tokens.BottomPadding,
      horizontalPadding = tokens.HorizontalPadding,
      labeledTopPadding = tokens.LabeledTopPadding,
      labeledBottomPadding = tokens.LabeledBottomPadding,
      labeledHorizontalPadding = tokens.LabeledHorizontalPadding,
      labelTopPadding = tokens.LabelTopPadding,
      labelHorizontalPadding = tokens.LabelHorizontalPadding,
      flowLabelSpacing = RadioGroupStyleTokens.FlowLabelSpacing,
      verticalItemSpacing = RadioGroupStyleTokens.VerticalItemSpacing,
      horizontalItemSpacing = RadioGroupStyleTokens.HorizontalItemSpacing,
      containerShape = RadioGroupStyleTokens.ContainerShape.value,
      textStyle = tokens.TextStyle.value,
      textStyleLabel = tokens.TextStyleLabel.value,
    )
  }
}

private data class RadioGroupColors(
  val containerColor: Color,
  val textColor: Color,
  val labelTextColor: Color,
  val disabledTextColor: Color,
  val disabledLabelTextColor: Color,
  val dividerColor: Color,
  val indicatorColor: Color,
  val indicatorSelectedColor: Color,
  val indicatorDisabledColor: Color,
)

internal data class RadioGroupStyleInternal(
  val style: RadioGroupStyle,
  val topPadding: Dp,
  val bottomPadding: Dp,
  val horizontalPadding: Dp,
  val labeledTopPadding: Dp,
  val labeledBottomPadding: Dp,
  val labeledHorizontalPadding: Dp,
  val labelTopPadding: Dp,
  val labelHorizontalPadding: Dp,
  val flowLabelSpacing: Dp,
  val verticalItemSpacing: Dp,
  val horizontalItemSpacing: Dp,
  val containerShape: Shape,
  val textStyle: TextStyle,
  val textStyleLabel: TextStyle,
)

@Composable
private fun RadioGroup(
  options: List<RadioOption>,
  selectedOptionId: RadioOptionId?,
  onRadioOptionSelected: (RadioOptionId) -> Unit,
  colors: RadioGroupColors,
  style: RadioGroupStyleInternal,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    if (style.style is RadioGroupStyle.Labeled) {
      RadioSurface(style, colors) {
        Column {
          HedvigText(
            text = style.style.label,
            style = style.textStyleLabel,
            color = colors.labelTextColor,
            modifier = Modifier
              .padding(horizontal = style.labelHorizontalPadding)
              .padding(top = style.labelTopPadding),
          )
          Column(Modifier.selectableGroup()) {
            when (style.style) {
              is RadioGroupStyle.Labeled.HorizontalFlow -> {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(style.flowLabelSpacing),
                  verticalArrangement = Arrangement.spacedBy(style.flowLabelSpacing),
                  modifier = Modifier.optionPaddings(style, false),
                ) {
                  for (option in options) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val selected = option.id == selectedOptionId
                    RadioOption(
                      option = option,
                      selected = selected,
                      enabled = enabled,
                      colors = colors,
                      style = style,
                      interactionSource = interactionSource,
                      modifier = Modifier.optionSelectable(
                        onRadioOptionSelected = onRadioOptionSelected,
                        radioOptionId = option.id,
                        selected = selected,
                        enabled = enabled,
                        indication = noopRipple(),
                        interactionSource = interactionSource,
                      ),
                    )
                  }
                }
              }

              is RadioGroupStyle.Labeled.VerticalWithDivider -> {
                options.forEachIndexed { index, option ->
                  val selected = option.id == selectedOptionId
                  RadioOption(
                    option = option,
                    selected = selected,
                    enabled = enabled,
                    colors = colors,
                    style = style,
                    modifier = Modifier
                      .fillMaxWidth()
                      .optionSelectable(onRadioOptionSelected, option.id, selected, enabled)
                      .horizontalDivider(DividerPosition.Top, show = index != 0)
                      .optionPaddings(style, option.hasLabel),
                  )
                }
              }
            }
          }
        }
      }
    } else {
      HorizontalOrVerticalLayout(
        options = options,
        style = style,
        colors = colors,
        modifier = Modifier.selectableGroup(),
      ) { option ->
        val selected = option.id == selectedOptionId
        RadioOption(
          option = option,
          selected = selected,
          enabled = enabled,
          colors = colors,
          style = style,
          modifier = Modifier
            .fillMaxWidth()
            .optionSelectable(onRadioOptionSelected, option.id, selected, enabled)
            .optionPaddings(style, option.hasLabel),
        )
      }
    }
  }
}

@Composable
private fun HorizontalOrVerticalLayout(
  options: List<RadioOption>,
  style: RadioGroupStyleInternal,
  colors: RadioGroupColors,
  modifier: Modifier = Modifier,
  itemContent: @Composable (RadioOption) -> Unit,
) {
  Box(modifier) {
    if (style.style == RadioGroupStyle.Horizontal) {
      Row(horizontalArrangement = Arrangement.spacedBy(style.horizontalItemSpacing)) {
        for (option in options) {
          RadioSurface(style, colors, Modifier.weight(1f)) {
            itemContent(option)
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(style.verticalItemSpacing)) {
        for (option in options) {
          RadioSurface(style, colors) {
            itemContent(option)
          }
        }
      }
    }
  }
}

@Composable
private fun RadioSurface(
  style: RadioGroupStyleInternal,
  colors: RadioGroupColors,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Surface(
    shape = style.containerShape,
    color = colors.containerColor,
    contentColor = colors.textColor,
    modifier = modifier,
  ) {
    content()
  }
}

@Composable
private fun Modifier.optionSelectable(
  onRadioOptionSelected: (RadioOptionId) -> Unit,
  radioOptionId: RadioOptionId,
  selected: Boolean,
  enabled: Boolean,
  interactionSource: MutableInteractionSource? = null,
  indication: Indication? = null,
): Modifier {
  return selectable(
    selected = selected,
    interactionSource = interactionSource ?: remember { MutableInteractionSource() },
    indication = indication ?: LocalIndication.current,
    onClick = { onRadioOptionSelected(radioOptionId) },
    enabled = enabled,
    role = Role.RadioButton,
  )
}

private fun Modifier.optionPaddings(style: RadioGroupStyleInternal, hasLabel: Boolean): Modifier {
  return padding(top = if (hasLabel) style.labeledTopPadding else style.topPadding)
    .padding(bottom = if (hasLabel) style.labeledBottomPadding else style.bottomPadding)
    .padding(horizontal = if (hasLabel) style.labeledHorizontalPadding else style.horizontalPadding)
}

@Composable
private fun RadioOption(
  option: RadioOption,
  selected: Boolean,
  enabled: Boolean,
  colors: RadioGroupColors,
  style: RadioGroupStyleInternal,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource? = null,
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(style.horizontalItemSpacing),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (option.iconResource != null) {
        RadioOptionIcon(option.iconResource)
      }
      if (style.style.leftAlignedIndicator) {
        RadioSelectIndicator(selected, enabled, colors, interactionSource)
      }
      Column {
        HedvigText(option.text, style = style.textStyle, color = colors.textColor)
        if (option.label != null) {
          HedvigText(option.label, style = style.textStyleLabel, color = colors.labelTextColor)
        }
      }
    }
    if (!style.style.leftAlignedIndicator) {
      Spacer(Modifier.width(style.horizontalItemSpacing))
      RadioSelectIndicator(selected, enabled, colors, interactionSource)
    }
  }
}

@Composable
private fun RadioSelectIndicator(
  selected: Boolean,
  enabled: Boolean,
  colors: RadioGroupColors,
  interactionSource: MutableInteractionSource? = null,
) {
  Canvas(
    Modifier
      .size(indicatorSize)
      .then(
        if (interactionSource != null) {
          Modifier.indication(interactionSource, ripple(radius = indicatorSize / 2))
        } else {
          Modifier
        },
      ),
  ) {
    if (!selected) {
      val stokeWidth = 2.dp.toPx()
      drawCircle(
        color = colors.indicatorColor,
        radius = size.minDimension / 2.0f - stokeWidth / 2,
        style = Stroke(width = stokeWidth),
      )
    } else {
      val color = if (enabled) {
        colors.indicatorSelectedColor
      } else {
        colors.indicatorDisabledColor
      }
      val stokeWidth = 8.dp.toPx()
      drawCircle(
        color = color,
        radius = size.minDimension / 2.0f - stokeWidth / 2,
        style = Stroke(width = stokeWidth),
      )
    }
  }
}

@Composable
private fun RadioOptionIcon(iconResource: IconResource) {
  Box(Modifier.size(32.dp), propagateMinConstraints = true) {
    when (iconResource) {
      is IconResource.Vector -> {
        Image(
          imageVector = iconResource.imageVector,
          contentDescription = EmptyContentDescription,
        )
      }

      is IconResource.Painter -> {
        Image(
          painter = painterResource(iconResource.painterResId),
          contentDescription = EmptyContentDescription,
        )
      }
    }
  }
}

private val RadioGroupStyle.leftAlignedIndicator: Boolean
  get() = this == RadioGroupStyle.LeftAligned || this is RadioGroupStyle.Labeled.HorizontalFlow

private val RadioOption.hasLabel: Boolean
  get() = label != null

internal object RadioGroupColorTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val OptionTextColor = ColorSchemeKeyTokens.TextPrimary
  val LabelTextColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val DisabledOptionTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DisabledLabelTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DividerColor = ColorSchemeKeyTokens.BorderPrimary
  val IndicatorColor = ColorSchemeKeyTokens.BorderSecondary
  val IndicatorSelectedColor = ColorSchemeKeyTokens.SignalGreenElement
  val IndicatorDisabledColor = ColorSchemeKeyTokens.FillDisabled
}

internal object RadioGroupStyleTokens {
  val FlowLabelSpacing: Dp = 16.dp
  val VerticalItemSpacing: Dp = 4.dp
  val HorizontalItemSpacing: Dp = 8.dp
  val ContainerShape: ShapeKeyTokens = ShapeKeyTokens.CornerLarge
}

@Suppress("PropertyName")
internal sealed interface RadioGroupSizeTokens {
  val TopPadding: Dp
  val BottomPadding: Dp
  val HorizontalPadding: Dp
  val LabeledTopPadding: Dp
  val LabeledBottomPadding: Dp
  val LabeledHorizontalPadding: Dp
  val LabelTopPadding: Dp
  val LabelHorizontalPadding: Dp
  val TextStyle: TypographyKeyTokens
  val TextStyleLabel: TypographyKeyTokens

  object Large : RadioGroupSizeTokens {
    override val TopPadding: Dp = 16.dp
    override val BottomPadding: Dp = 18.dp
    override val HorizontalPadding: Dp = 16.dp
    override val LabeledTopPadding: Dp = 7.dp
    override val LabeledBottomPadding: Dp = 10.dp
    override val LabeledHorizontalPadding: Dp = 16.dp
    override val LabelTopPadding: Dp = 12.dp
    override val LabelHorizontalPadding: Dp = 16.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodyMedium
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }

  object Medium : RadioGroupSizeTokens {
    override val TopPadding: Dp = 16.dp
    override val BottomPadding: Dp = 18.dp
    override val HorizontalPadding: Dp = 16.dp
    override val LabeledTopPadding: Dp = 10.dp
    override val LabeledBottomPadding: Dp = 12.dp
    override val LabeledHorizontalPadding: Dp = 16.dp
    override val LabelTopPadding: Dp = 12.dp
    override val LabelHorizontalPadding: Dp = 16.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodySmall
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }

  object Small : RadioGroupSizeTokens {
    override val TopPadding: Dp = 15.dp
    override val BottomPadding: Dp = 17.dp
    override val HorizontalPadding: Dp = 14.dp
    override val LabeledTopPadding: Dp = 14.dp
    override val LabeledBottomPadding: Dp = 17.dp
    override val LabeledHorizontalPadding: Dp = 14.dp
    override val LabelTopPadding: Dp = 10.dp
    override val LabelHorizontalPadding: Dp = 14.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodySmall
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }
}

private val indicatorSize = 24.dp
