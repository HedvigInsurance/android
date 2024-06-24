package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupSize
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupSize.Large
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupSize.Medium
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupSize.Small
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupStyle
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupStyle.Vertical
import com.hedvig.android.design.system.hedvig.CheckboxGroupDefaults.CheckboxGroupStyle.VerticalWithGroupLabel
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.IconResource.Painter
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.LargeSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.MediumSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.SmallSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.LargeSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.MediumSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.SmallSizeCheckboxTokens
import hedvig.resources.R

@Composable
fun CheckboxGroup(
  data: List<OptionData>,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  groupLockedState: LockedState = NotLocked,
  groupStyle: CheckboxGroupStyle = CheckboxGroupDefaults.checkboxGroupStyle,
  groupSize: CheckboxGroupSize = CheckboxGroupDefaults.checkboxGroupSize,
) {
  val contentPadding = calculateContentPadding(groupStyle, groupSize)
  when (groupStyle) {
    is Vertical -> VerticalCheckboxGroup(
      data = data,
      checkboxGroupStyle = groupStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      checkboxGroupSize = groupSize,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )

    is VerticalWithGroupLabel -> VerticalCheckboxGroupWithLabel(
      data = data,
      checkboxGroupStyle = groupStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      checkboxGroupSize = groupSize,
      contentPaddingValues = contentPadding,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )
  }
}

@Composable
private fun VerticalCheckboxGroup(
  data: List<OptionData>,
  checkboxGroupStyle: Vertical,
  groupLockedState: LockedState,
  checkboxGroupSize: CheckboxGroupSize,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    for (optionData in data) {
      val checkboxStyle = when (checkboxGroupStyle) {
        is Vertical.Default -> CheckboxStyle.Default
        is Vertical.Icon -> CheckboxStyle.Icon(
          optionData.iconResource ?: Painter(R.drawable.pillow_hedvig),
        ) // todo: how is that for placeholder
        is Vertical.Label -> CheckboxStyle.Label(optionData.labelText ?: "") // todo: no placeholder
        is Vertical.LeftAligned -> CheckboxStyle.LeftAligned
      }
      Checkbox(
        data = optionData,
        checkboxStyle = checkboxStyle,
        groupLockedState = groupLockedState,
        checkboxSize = checkboxGroupSize.toOptionSize(),
        onClick = {
          onOptionClick(optionData.id)
        },
      )
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun VerticalCheckboxGroupWithLabel(
  data: List<OptionData>,
  checkboxGroupStyle: VerticalWithGroupLabel,
  groupLockedState: LockedState,
  checkboxGroupSize: CheckboxGroupSize,
  contentPaddingValues: PaddingValues,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = checkboxGroupSize.getShape(),
    color = checkboxColors.containerColor,
  ) {
    Column(
      modifier,
    ) {
      val labelTextColor = checkboxColors.labelTextColor(groupLockedState)
      HedvigText(
        modifier = Modifier.padding(contentPaddingValues),
        text = checkboxGroupStyle.groupLabelText,
        style = MediumSizeCheckboxTokens.LabelTextFont.value, // same for all sizes in figma
        color = labelTextColor,
      )
      Column(modifier) {
        for (checkboxData in data) {
          val interactionSource = remember { MutableInteractionSource() }
          val modifierRipple = Modifier
            .clickable(
              enabled = calculateLockedStateForItemInGroup(checkboxData, groupLockedState) == NotLocked,
              role = Role.Checkbox,
              interactionSource = interactionSource,
              indication = ripple(
                bounded = true,
              ),
              onClick = {
                onOptionClick(checkboxData.id)
              },
            )
          val checkboxStyle = when (checkboxGroupStyle) {
            is CheckboxGroupStyle.VerticalWithGroupLabel.Default -> CheckboxStyle.Default
            is CheckboxGroupStyle.VerticalWithGroupLabel.Icon -> CheckboxStyle.Icon(
              checkboxData.iconResource ?: Painter(R.drawable.pillow_hedvig),
            ) // todo: how is that for placeholder
            is CheckboxGroupStyle.VerticalWithGroupLabel.Label -> CheckboxStyle.Label(
              checkboxData.labelText ?: "",
            ) // todo: no placeholder
            is CheckboxGroupStyle.VerticalWithGroupLabel.LeftAligned -> CheckboxStyle.LeftAligned
          }
          Checkbox(
            data = checkboxData,
            checkboxStyle = checkboxStyle,
            groupLockedState = groupLockedState,
            checkboxSize = checkboxGroupSize.toOptionSize(),
            interactionSource = interactionSource,
            modifier = modifierRipple,
          )
          if (data.indexOf(checkboxData) != data.lastIndex) {
            HorizontalDivider()
          }
        }
      }
    }
  }
}

private fun calculateContentPadding(
  checkboxGroupStyle: CheckboxGroupStyle,
  checkboxGroupSize: CheckboxGroupSize,
): PaddingValues {
  val paddingValuesForLabel = when (checkboxGroupSize) {
    Large -> PaddingValues(
      start = LargeSizeCheckboxGroupTokens.HorizontalPadding,
      end = LargeSizeCheckboxGroupTokens.HorizontalPadding,
      top = LargeSizeCheckboxGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel) {
        LargeSizeCheckboxGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )

    Medium -> PaddingValues(
      start = MediumSizeCheckboxGroupTokens.HorizontalPadding,
      end = MediumSizeCheckboxGroupTokens.HorizontalPadding,
      top = MediumSizeCheckboxGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel) {
        MediumSizeCheckboxGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )

    Small -> PaddingValues(
      start = SmallSizeCheckboxGroupTokens.HorizontalPadding,
      end = SmallSizeCheckboxGroupTokens.HorizontalPadding,
      top = SmallSizeCheckboxGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel) {
        SmallSizeCheckboxGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )
  }
  return when (checkboxGroupStyle) {
    is CheckboxGroupStyle.Vertical -> PaddingValues()
    is CheckboxGroupStyle.VerticalWithGroupLabel -> paddingValuesForLabel
  }
}

private fun CheckboxGroupSize.toOptionSize(): CheckboxDefaults.CheckboxSize {
  return when (this) {
    Large -> CheckboxDefaults.CheckboxSize.Large
    Medium -> CheckboxDefaults.CheckboxSize.Medium
    Small -> CheckboxDefaults.CheckboxSize.Small
  }
}

@Composable
private fun CheckboxGroupSize.getShape(): Shape {
  return when (this) {
    Large -> LargeSizeCheckboxTokens.ContainerShape.value
    Medium -> MediumSizeCheckboxTokens.ContainerShape.value
    Small -> SmallSizeCheckboxTokens.ContainerShape.value
  }
}

object CheckboxGroupDefaults {
  internal val checkboxGroupStyle: CheckboxGroupStyle = CheckboxGroupStyle.Vertical.Default
  internal val checkboxGroupSize: CheckboxGroupSize = Large

  sealed interface CheckboxGroupStyle {
    sealed interface Vertical : CheckboxGroupStyle {
      data object Default : Vertical

      data object Label : Vertical

      data object Icon : Vertical

      data object LeftAligned : Vertical
    }

    sealed interface VerticalWithGroupLabel : CheckboxGroupStyle {
      val groupLabelText: String

      data class Default(override val groupLabelText: String) : VerticalWithGroupLabel

      data class Label(override val groupLabelText: String) : VerticalWithGroupLabel

      data class Icon(override val groupLabelText: String) : VerticalWithGroupLabel

      data class LeftAligned(override val groupLabelText: String) : VerticalWithGroupLabel
    }
  }

  enum class CheckboxGroupSize {
    Large,
    Medium,
    Small,
  }
}

@Preview
@Composable
private fun PreviewCheckboxStyles(
  @PreviewParameter(CheckboxGroupStyleProvider::class) style: CheckboxGroupStyle,
) {
  HedvigTheme(darkTheme = false) {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(Modifier.padding(16.dp)) {
        CheckboxGroup(
          groupStyle = style,
          groupSize = Medium,
          data = listOf(
            OptionData(
              id = "1",
              optionText = "Option text 1",
              labelText = "Label text 1",
              iconResource = IconResource.Vector(HedvigIcons.FlagSweden),
              chosenState = Chosen,
            ),
            OptionData(
              id = "2",
              optionText = "Option text 2",
              labelText = "Label text2",
              iconResource = IconResource.Vector(HedvigIcons.FlagSweden),
              chosenState = NotChosen,
            ),
            OptionData(
              id = "3",
              optionText = "Option text 3",
              labelText = "Label text 3",
              iconResource = IconResource.Vector(HedvigIcons.FlagSweden),
              chosenState = NotChosen,
            ),
            OptionData(
              id = "4",
              optionText = "Option text 4",
              labelText = "Label text 4",
              iconResource = IconResource.Vector(HedvigIcons.FlagSweden),
              chosenState = Chosen,
            ),
          ),
          onOptionClick = {},
        )
      }
    }
  }
}

internal class CheckboxGroupStyleProvider :
  CollectionPreviewParameterProvider<CheckboxGroupStyle>(
    listOf(
      Vertical.Default,
      Vertical.Icon,
      Vertical.Label,
      Vertical.LeftAligned,
      VerticalWithGroupLabel.Default("Group label"),
      VerticalWithGroupLabel.Icon("Group label"),
      VerticalWithGroupLabel.Label("Group label"),
      VerticalWithGroupLabel.LeftAligned("Group label"),
    ),
  )
