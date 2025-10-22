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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.RadioGroupColorTokens
import com.hedvig.android.design.system.hedvig.tokens.RadioGroupSizeTokens
import com.hedvig.android.design.system.hedvig.tokens.RadioGroupStyleTokens

data class RadioOption(
  val id: RadioOptionId,
  val text: String,
  val label: String? = null,
  val iconResource: IconResource? = null,
)

data class CheckboxOption(
  val text: String,
  val label: String? = null,
  val iconResource: IconResource? = null,
)

@JvmInline
value class RadioOptionId(val id: String)

enum class RadioGroupSize {
  Large,
  Medium,
  Small,
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
  modifier: Modifier = Modifier,
  size: RadioGroupSize = RadioGroupSize.Medium,
  style: RadioGroupStyle = RadioGroupStyle.Vertical,
  colors: RadioGroupColors = RadioGroupDefaults.colors,
  disabledOptions: List<RadioOptionId> = emptyList(),
  enabled: Boolean = true,
  textEndContent: @Composable ((RadioOptionId) -> Unit)? = null,
) {
  val spacings = RadioGroupDefaults.style(size, style)
  RadioGroup(
    options = options,
    onRadioOptionSelected = onRadioOptionSelected,
    selectedOptionIds = listOfNotNull(selectedOption),
    colors = colors,
    style = spacings,
    disabledOptions = disabledOptions,
    enabled = enabled,
    textEndContent = textEndContent,
    selectIndicator = { selected, enabled, colors, interactionSource ->
      RadioSelectIndicator(
        selected = selected,
        enabled = enabled,
        colors = colors,
        style = spacings,
        interactionSource = interactionSource,
      )
    },
    modifier = modifier,
  )
}

@Composable
fun Checkbox(
  option: CheckboxOption,
  selected: Boolean,
  onCheckboxSelected: () -> Unit,
  modifier: Modifier = Modifier,
  size: RadioGroupSize = RadioGroupSize.Medium,
  style: RadioGroupStyle = RadioGroupStyle.Vertical,
  colors: RadioGroupColors = RadioGroupDefaults.colors,
  enabled: Boolean = true,
) {
  val id = RadioOptionId("1")
  CheckboxGroup(
    options = listOf(RadioOption(id, option.text, option.label, option.iconResource)),
    selectedOptions = if (selected) listOf(id) else emptyList(),
    onRadioOptionSelected = { onCheckboxSelected() },
    modifier = modifier,
    size = size,
    style = style,
    colors = colors,
    enabled = enabled,
  )
}

@Composable
fun CheckboxGroup(
  options: List<RadioOption>,
  selectedOptions: List<RadioOptionId>,
  onRadioOptionSelected: (RadioOptionId) -> Unit,
  modifier: Modifier = Modifier,
  size: RadioGroupSize = RadioGroupSize.Medium,
  style: RadioGroupStyle = RadioGroupStyle.Vertical,
  colors: RadioGroupColors = RadioGroupDefaults.colors,
  disabledOptions: List<RadioOptionId> = emptyList(),
  enabled: Boolean = true,
) {
  val spacings = RadioGroupDefaults.style(size, style)
  RadioGroup(
    options = options,
    onRadioOptionSelected = onRadioOptionSelected,
    selectedOptionIds = selectedOptions,
    colors = colors,
    style = spacings,
    disabledOptions = disabledOptions,
    enabled = enabled,
    selectIndicator = { selected, enabled, colors, interactionSource ->
      CheckboxSelectIndicator(
        selected = selected,
        enabled = enabled,
        colors = colors,
        style = spacings,
        interactionSource = interactionSource,
      )
    },
    modifier = modifier,
  )
}

object RadioGroupDefaults {
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
  internal fun style(size: RadioGroupSize, style: RadioGroupStyle): RadioGroupStyleInternal {
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
      textToLabelSpacing = RadioGroupStyleTokens.TextToLabelSpacing,
      textStyle = tokens.TextStyle.value,
      textStyleLabel = tokens.TextStyleLabel.value,
      indicatorSize = RadioGroupStyleTokens.IndicatorSize,
    )
  }
}

data class RadioGroupColors(
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
  val textToLabelSpacing: Dp,
  val textStyle: TextStyle,
  val textStyleLabel: TextStyle,
  val indicatorSize: Dp,
)

@Composable
private fun RadioGroup(
  options: List<RadioOption>,
  selectedOptionIds: List<RadioOptionId>,
  onRadioOptionSelected: (RadioOptionId) -> Unit,
  colors: RadioGroupColors,
  style: RadioGroupStyleInternal,
  selectIndicator: SelectIndicator,
  modifier: Modifier = Modifier,
  disabledOptions: List<RadioOptionId> = emptyList(),
  enabled: Boolean = true,
  textEndContent: @Composable ((RadioOptionId) -> Unit)? = null,
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
                    val selected = option.id in selectedOptionIds
                    val enabled = enabled && option.id !in disabledOptions
                    RadioOption(
                      option = option,
                      selected = selected,
                      enabled = enabled,
                      colors = colors,
                      style = style,
                      interactionSource = interactionSource,
                      selectIndicator = selectIndicator,
                      textEndContent = textEndContent,
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
                  val selected = option.id in selectedOptionIds
                  val enabled = enabled && option.id !in disabledOptions
                  RadioOption(
                    option = option,
                    selected = selected,
                    enabled = enabled,
                    colors = colors,
                    style = style,
                    selectIndicator = selectIndicator,
                    textEndContent = textEndContent,
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
        val selected = option.id in selectedOptionIds
        val enabled = enabled && option.id !in disabledOptions
        RadioOption(
          option = option,
          selected = selected,
          enabled = enabled,
          colors = colors,
          style = style,
          selectIndicator = selectIndicator,
          textEndContent = textEndContent,
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
  selectIndicator: SelectIndicator,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource? = null,
  textEndContent: @Composable ((RadioOptionId) -> Unit)? = null,
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
        selectIndicator(selected, enabled, colors, interactionSource)
      }
      Column {
        val textComposable: @Composable () -> Unit = {
          HedvigText(option.text, style = style.textStyle, color = colors.textColor)
        }
        if (textEndContent != null) {
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = { textComposable() },
            endSlot = { Box(Modifier.wrapContentSize(Alignment.TopEnd)) { textEndContent(option.id) } },
            spaceBetween = style.horizontalItemSpacing,
          )
        } else {
          textComposable()
        }
        if (option.label != null) {
          Spacer(Modifier.height(style.textToLabelSpacing))
          HedvigText(option.label, style = style.textStyleLabel, color = colors.labelTextColor)
        }
      }
    }
    if (!style.style.leftAlignedIndicator) {
      Spacer(Modifier.width(style.horizontalItemSpacing))
      selectIndicator(selected, enabled, colors, interactionSource)
    }
  }
}

private typealias SelectIndicator = @Composable (
  selected: Boolean,
  enabled: Boolean,
  colors: RadioGroupColors,
  interactionSource: MutableInteractionSource?,
) -> Unit

@Composable
private fun RadioSelectIndicator(
  selected: Boolean,
  enabled: Boolean,
  colors: RadioGroupColors,
  style: RadioGroupStyleInternal,
  interactionSource: MutableInteractionSource? = null,
) {
  Canvas(
    Modifier
      .size(style.indicatorSize)
      .then(
        if (interactionSource != null) {
          Modifier.indication(interactionSource, ripple(radius = style.indicatorSize / 2))
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
private fun CheckboxSelectIndicator(
  selected: Boolean,
  enabled: Boolean,
  colors: RadioGroupColors,
  style: RadioGroupStyleInternal,
  interactionSource: MutableInteractionSource? = null,
) {
  val shape = HedvigTheme.shapes.cornerXSmall
  val checkmarkVector = rememberVectorPainter(
    image = HedvigIcons.Checkmark,
  )
  val checkmarkTint = colors.containerColor
  Canvas(
    Modifier
      .size(style.indicatorSize)
      .clip(shape)
      .then(
        if (interactionSource != null) {
          Modifier.indication(interactionSource, ripple())
        } else {
          Modifier
        },
      ),
  ) {
    if (!selected) {
      drawOutline(
        color = colors.indicatorColor,
        outline = shape.createOutline(size, layoutDirection, this),
        style = Stroke(width = 4.dp.toPx()),
      )
    } else {
      val color = if (enabled) {
        colors.indicatorSelectedColor
      } else {
        colors.indicatorDisabledColor
      }
      drawRect(color = color)
      with(checkmarkVector) {
        draw(checkmarkVector.intrinsicSize, colorFilter = ColorFilter.tint(checkmarkTint))
      }
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
  get() = this is RadioGroupStyle.LeftAligned || this is RadioGroupStyle.Labeled.HorizontalFlow

private val RadioOption.hasLabel: Boolean
  get() = label != null
