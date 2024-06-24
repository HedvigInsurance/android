package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.FlowRowOverflow.Companion
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.IconResource.Painter
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Large
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Medium
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupSize.Small
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.Horizontal
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.HorizontalWithLabel
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.Vertical
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.VerticalWithGroupLabel
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Default
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.LargeSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.MediumSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.SmallSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.LargeSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.SmallSizeRadioOptionTokens
import hedvig.resources.R

@Composable
fun RadioGroup(
  data: List<RadioOptionData>,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  groupLockedState: LockedState = NotLocked,
  radioGroupStyle: RadioGroupStyle = RadioGroupDefaults.radioGroupStyle,
  radioGroupSize: RadioGroupSize = RadioGroupDefaults.radioGroupSize,
) {
  val contentPadding = calculateContentPadding(radioGroupStyle, radioGroupSize)
  when (radioGroupStyle) {
    Horizontal -> HorizontalRadioGroup(
      data = data,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )

    is HorizontalWithLabel -> HorizontalRadioGroupWithLabel(
      data = data,
      groupLabelText = radioGroupStyle.groupLabelText,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      contentPaddingValues = contentPadding,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )

    is Vertical -> VerticalRadioGroup(
      data = data,
      radioGroupStyle = radioGroupStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )

    is VerticalWithGroupLabel -> VerticalRadioGroupWithLabel(
      data = data,
      radioGroupStyle = radioGroupStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      contentPaddingValues = contentPadding,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HorizontalRadioGroup(
  data: List<RadioOptionData>,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier,
    maxItemsInEachRow = 2,
    overflow = Companion.Visible,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
  ) {
    for (i in data) {
      val itemPadding = if (data.indexOf(i) % 2 != 0) {
        PaddingValues()
      } else {
        PaddingValues(end = 4.dp)
      }
      if (data.indexOf(i) % 2 == 0 && data.indexOf(i) == data.lastIndex) {
        RadioOption(
          data = i,
          radioOptionStyle = LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
          onOptionClick = {
            onOptionClick(i.id)
          },
          modifier = Modifier
            .weight(1f)
            .width(Min)
            .padding(itemPadding),
          // so with this implementation the downside is
          // that both types of horizontal groups are not good for optionText longer than 1 word.
        )
        Spacer(Modifier.weight(1f))
      } else {
        RadioOption(
          data = i,
          radioOptionStyle = LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
          onOptionClick = {
            onOptionClick(i.id)
          },
          modifier = Modifier
            .weight(1f)
            .width(Min)
            .padding(itemPadding),
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HorizontalRadioGroupWithLabel(
  data: List<RadioOptionData>,
  groupLabelText: String,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  contentPaddingValues: PaddingValues,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = radioGroupSize.toOptionSize().size(LeftAligned).shape
  Surface(
    shape = shape,
    color = radioOptionColors.containerColor,
    modifier = modifier,
  ) {
    Column(
      Modifier
        .padding(
          contentPaddingValues,
        ),
    ) {
      val labelTextColor = radioOptionColors.labelTextColor(groupLockedState)
      HedvigText(
        text = groupLabelText,
        style = calculateHorizontalGroupLabelTextStyle(radioGroupSize),
        color = labelTextColor,
      )
      Spacer(Modifier.height(16.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 2,
        overflow = FlowRowOverflow.Visible,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        for (option in data) {
          val optionTextColor = radioOptionColors.optionTextColor(option.lockedState)
          val itemModifier = if (data.size == 2) {
            Modifier.width(Min)
          } else {
            Modifier
              .weight(1f)
              .width(Min)
          }
          val lockedState = calculateLockedStateForItemInGroup(option, groupLockedState)
          HorizontalWithLabelRadioOption(
            enabled = lockedState == NotLocked,
            style = calculateHorizontalGroupOptionTextStyle(radioGroupSize),
            textColor = optionTextColor,
            modifier = itemModifier
              .align(Alignment.CenterVertically),
            optionText = option.optionText,
            onClick = {
              onOptionClick(option.id)
            },
            chosenState = option.chosenState,
            lockedState = lockedState,
          )
        }
      }
    }
  }
}

@Composable
private fun HorizontalWithLabelRadioOption(
  optionText: String,
  onClick: () -> Unit,
  lockedState: LockedState,
  chosenState: ChosenState,
  enabled: Boolean,
  style: TextStyle,
  textColor: Color,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable(
        enabled = enabled,
        role = Role.RadioButton,
        interactionSource = interactionSource,
        indication = ripple(
          bounded = false,
          radius = 50.dp,
          // todo: pls check out this ripple. I like this one, but maybe it's too much
        ),
      ) {
        if (enabled) {
          onClick()
        }
      },
  ) {
    SelectIndicationCircle(
      chosenState,
      lockedState,
    )
    Spacer(Modifier.width(8.dp))
    HedvigText(
      optionText,
      style = style,
      color = textColor,
      modifier = Modifier,
    )
  }
}

@Composable
private fun VerticalRadioGroup(
  data: List<RadioOptionData>,
  radioGroupStyle: Vertical,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    for (radioOptionData in data) {
      val radioOptionStyle = when (radioGroupStyle) {
        is Vertical.Default -> Default
        is Vertical.Icon -> RadioOptionStyle.Icon(radioOptionData.iconResource ?: Painter(R.drawable.pillow_hedvig)) // todo: how is that for placeholder
        is Vertical.Label -> Label(radioOptionData.labelText ?: "") // todo: no placeholder
        is Vertical.LeftAligned -> LeftAligned
      }
      RadioOption(
        data = radioOptionData,
        radioOptionStyle = radioOptionStyle,
        groupLockedState = groupLockedState,
        radioOptionSize = radioGroupSize.toOptionSize(),
        onOptionClick = {
          onOptionClick(radioOptionData.id)
        },
      )
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun VerticalRadioGroupWithLabel(
  data: List<RadioOptionData>,
  radioGroupStyle: VerticalWithGroupLabel,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  contentPaddingValues: PaddingValues,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = radioGroupSize.getShape(),
    color = radioOptionColors.containerColor,
  ) {
    Column(
      modifier,
    ) {
      val labelTextColor = radioOptionColors.labelTextColor(groupLockedState)
      HedvigText(
        modifier = Modifier.padding(contentPaddingValues),
        text = radioGroupStyle.groupLabelText,
        style = MediumSizeRadioOptionTokens.LabelTextFont.value, // same for all sizes in figma
        color = labelTextColor,
      )
      Column(modifier) {
        for (radioOptionData in data) {
          val interactionSource = remember { MutableInteractionSource() }
          val modifierRipple = Modifier
            .clickable(
              enabled = calculateLockedStateForItemInGroup(radioOptionData, groupLockedState) == NotLocked,
              role = Role.RadioButton,
              interactionSource = interactionSource,
              indication = ripple(
                bounded = true,
              ),
              onClick = {
                onOptionClick(radioOptionData.id)
              },
            )
          val radioOptionStyle = when (radioGroupStyle) {
            is VerticalWithGroupLabel.Default -> Default
            is VerticalWithGroupLabel.Icon -> RadioOptionStyle.Icon(
              radioOptionData.iconResource ?: Painter(R.drawable.pillow_hedvig),
            ) // todo: how is that for placeholder
            is VerticalWithGroupLabel.Label -> Label(radioOptionData.labelText ?: "") // todo: no placeholder
            is VerticalWithGroupLabel.LeftAligned -> LeftAligned
          }
          RadioOption(
            data = radioOptionData,
            radioOptionStyle = radioOptionStyle,
            groupLockedState = groupLockedState,
            radioOptionSize = radioGroupSize.toOptionSize(),
            interactionSource = interactionSource,
            modifier = modifierRipple,
          )
          if (data.indexOf(radioOptionData) != data.lastIndex) {
            HorizontalDivider()
          }
        }
      }
    }
  }
}

@Composable
private fun calculateHorizontalGroupLabelTextStyle(radioGroupSize: RadioGroupSize): TextStyle {
  return when (radioGroupSize) {
    Large -> LargeSizeRadioGroupTokens.LabelTextFont.value
    Medium -> MediumSizeRadioGroupTokens.LabelTextFont.value
    Small -> SmallSizeRadioGroupTokens.LabelTextFont.value
  }
}

@Composable
private fun calculateHorizontalGroupOptionTextStyle(radioGroupSize: RadioGroupSize): TextStyle {
  return when (radioGroupSize) {
    Large -> LargeSizeRadioGroupTokens.HorizontalOptionTextFont.value
    Medium -> MediumSizeRadioGroupTokens.HorizontalOptionTextFont.value
    Small -> SmallSizeRadioGroupTokens.HorizontalOptionTextFont.value
  }
}

private fun calculateContentPadding(radioGroupStyle: RadioGroupStyle, radioGroupSize: RadioGroupSize): PaddingValues {
  val paddingValuesForLabel = when (radioGroupSize) {
    Large -> PaddingValues(
      start = LargeSizeRadioGroupTokens.HorizontalPadding,
      end = LargeSizeRadioGroupTokens.HorizontalPadding,
      top = LargeSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (radioGroupStyle !is VerticalWithGroupLabel) {
        LargeSizeRadioGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )

    Medium -> PaddingValues(
      start = MediumSizeRadioGroupTokens.HorizontalPadding,
      end = MediumSizeRadioGroupTokens.HorizontalPadding,
      top = MediumSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (radioGroupStyle !is VerticalWithGroupLabel) {
        MediumSizeRadioGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )

    Small -> PaddingValues(
      start = SmallSizeRadioGroupTokens.HorizontalPadding,
      end = SmallSizeRadioGroupTokens.HorizontalPadding,
      top = SmallSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (radioGroupStyle !is VerticalWithGroupLabel) {
        SmallSizeRadioGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )
  }
  return when (radioGroupStyle) {
    Horizontal -> PaddingValues()
    is HorizontalWithLabel -> paddingValuesForLabel
    is Vertical -> PaddingValues()
    is VerticalWithGroupLabel -> paddingValuesForLabel
  }
}

private fun RadioGroupSize.toOptionSize(): RadioOptionDefaults.RadioOptionSize {
  return when (this) {
    Large -> RadioOptionDefaults.RadioOptionSize.Large
    Medium -> RadioOptionDefaults.RadioOptionSize.Medium
    Small -> RadioOptionDefaults.RadioOptionSize.Small
  }
}

@Composable
private fun RadioGroupSize.getShape(): Shape {
  return when (this) {
    Large -> LargeSizeRadioOptionTokens.ContainerShape.value
    Medium -> MediumSizeRadioOptionTokens.ContainerShape.value
    Small -> SmallSizeRadioOptionTokens.ContainerShape.value
  }
}

object RadioGroupDefaults {
  internal val radioGroupStyle: RadioGroupStyle = Vertical.Default
  internal val radioGroupSize: RadioGroupSize = Large

  sealed interface RadioGroupStyle {
    data object Horizontal : RadioGroupStyle

    data class HorizontalWithLabel(
      val groupLabelText: String,
    ) : RadioGroupStyle

    sealed interface Vertical : RadioGroupStyle {
      data object Default : Vertical

      data object Label : Vertical

      data object Icon : Vertical

      data object LeftAligned : Vertical
    }

    sealed interface VerticalWithGroupLabel : RadioGroupStyle {
      val groupLabelText: String

      data class Default(override val groupLabelText: String) : VerticalWithGroupLabel

      data class Label(override val groupLabelText: String) : VerticalWithGroupLabel

      data class Icon(override val groupLabelText: String) : VerticalWithGroupLabel

      data class LeftAligned(override val groupLabelText: String) : VerticalWithGroupLabel
    }
  }

  enum class RadioGroupSize {
    Large,
    Medium,
    Small,
  }
}

@Preview
@Composable
fun GroupPreview(
  @PreviewParameter(GroupSizeParametersProvider::class) size: RadioGroupSize,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(Modifier.padding(horizontal = 16.dp)) {
        HedvigText("Horizontal")
        HorizontalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier,
          onOptionClick = {},
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Yes",
              chosenState = Chosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(6.dp))
        HedvigText("Horizontal locked group")
        HorizontalRadioGroup(
          groupLockedState = Locked,
          radioGroupSize = size,
          modifier = Modifier,
          onOptionClick = {},
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Yes",
              chosenState = Chosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(6.dp))
        HedvigText("Horizontal with long List")
        HorizontalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier,
          onOptionClick = {},
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Yes",
              chosenState = Chosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "Non",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "Maybe",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "Not sure",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "Probably",
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(16.dp))
        HedvigText("Horizontal with label")
        HorizontalRadioGroupWithLabel(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier,
          groupLabelText = "Label",
          onOptionClick = {},
          contentPaddingValues = calculateContentPadding(HorizontalWithLabel("Label"), size),
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Yes",
              chosenState = Chosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(4.dp))
        HedvigText("Horizontal with label and long List")
        Row {
          HorizontalRadioGroupWithLabel(
            groupLockedState = NotLocked,
            radioGroupSize = size,
            modifier = Modifier.fillMaxWidth(),
            groupLabelText = "Label",
            onOptionClick = {},
            contentPaddingValues = calculateContentPadding(HorizontalWithLabel("Label"), size),
            data = listOf(
              RadioOptionData(
                id = "",
                optionText = "Yes",
                chosenState = Chosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "No",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Maybe",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Not sure ",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Perhaps",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Very unlikely",
                chosenState = NotChosen,
              ),
            ),
          )
        }
        HedvigText("Vertical")
        Column(Modifier.verticalScroll(rememberScrollState())) {
          VerticalRadioGroup(
            groupLockedState = NotLocked,
            radioGroupSize = size,
            modifier = Modifier.fillMaxWidth(),
            radioGroupStyle = Vertical.Default,
            onOptionClick = {},
            data = listOf(
              RadioOptionData(
                id = "",
                optionText = "Yes",
                chosenState = Chosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "No",
                chosenState = NotChosen,
                lockedState = Locked,
              ),
              RadioOptionData(
                id = "",
                optionText = "Maybe",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Not sure",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Perhaps",
                chosenState = NotChosen,
              ),
              RadioOptionData(
                id = "",
                optionText = "Very unlikely",
                chosenState = NotChosen,
              ),
            ),
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun VerticalGroupsPreview(
  @PreviewParameter(GroupSizeParametersProvider::class) size: RadioGroupSize,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(Modifier.padding(horizontal = 16.dp)) {
        HedvigText("Vertical")
        VerticalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier.fillMaxWidth(),
          radioGroupStyle = Vertical.Default,
          onOptionClick = {},
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = Chosen,
              lockedState = NotLocked,
            ),
          ),
        )
        Spacer(Modifier.height(16.dp))
        HedvigText("Vertical with label")
        VerticalRadioGroupWithLabel(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier.fillMaxWidth(),
          contentPaddingValues = calculateContentPadding(
            VerticalWithGroupLabel.Default("Label"),
            size,
          ),
          onOptionClick = {},
          radioGroupStyle = VerticalWithGroupLabel.Default("Label"),
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = Chosen,
              lockedState = NotLocked,
            ),
          ),
        )
      }
    }
  }
}

@Preview
@Composable
fun VerticalGroupWithDiffOptionStylesPreview(
  @PreviewParameter(
    RadioGroupStyleVerticalGroupLabelParameterProvider::class,
  ) style: VerticalWithGroupLabel,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier
          .padding(horizontal = 16.dp)
          .verticalScroll(rememberScrollState()),
      ) {
        VerticalRadioGroupWithLabel(
          groupLockedState = NotLocked,
          radioGroupSize = Small,
          modifier = Modifier.fillMaxWidth(),
          radioGroupStyle = style,
          contentPaddingValues = calculateContentPadding(
            VerticalWithGroupLabel.Default("GroupLabel"),
            Small,
          ),
          onOptionClick = {},
          data = listOf(
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = Chosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              id = "",
              optionText = "Vertical option",
              chosenState = NotChosen,
            ),
            RadioOptionData(
              id = "",
              optionText = "No",
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
          ),
        )
      }
    }
  }
}

private class GroupSizeParametersProvider :
  CollectionPreviewParameterProvider<RadioGroupSize>(
    listOf(
      Large,
      Medium,
      Small,
    ),
  )

private class RadioGroupStyleVerticalGroupLabelParameterProvider :
  CollectionPreviewParameterProvider<VerticalWithGroupLabel>(
    listOf(
      VerticalWithGroupLabel.Default("GroupLabel"),
      VerticalWithGroupLabel.Label("GroupLabel"),
      VerticalWithGroupLabel.LeftAligned("GroupLabel"),
      VerticalWithGroupLabel.Icon("GroupLabel"),
    ),
  )
