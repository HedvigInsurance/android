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
import androidx.compose.ui.text.TextStyle
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
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithIcon
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataWithLabel
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.LargeSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.MediumSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxGroupTokens.SmallSizeCheckboxGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.LargeSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.MediumSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.SmallSizeCheckboxTokens

@Composable
fun CheckboxGroup(
  groupStyle: CheckboxGroupStyle,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  groupLockedState: LockedState = NotLocked,
  groupSize: CheckboxGroupSize = CheckboxGroupDefaults.checkboxGroupSize,
) {
  val contentPadding = calculateContentPadding(groupStyle, groupSize)
  when (groupStyle) {
    is Vertical<*> -> VerticalCheckboxGroup(
      checkboxGroupStyle = groupStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      checkboxGroupSize = groupSize,
      onOptionClick = { id ->
        onOptionClick(id)
      },
    )

    is VerticalWithGroupLabel<*> -> VerticalCheckboxGroupWithLabel(
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
  checkboxGroupStyle: Vertical<*>,
  groupLockedState: LockedState,
  checkboxGroupSize: CheckboxGroupSize,
  onOptionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    for (data in checkboxGroupStyle.dataList) {
      val checkboxStyle = when (checkboxGroupStyle) {
        is Vertical.Default -> CheckboxStyle.Default
        is Vertical.Icon -> CheckboxStyle.Icon(
          (data as RadioOptionGroupDataWithIcon).iconResource,
        )

        is Vertical.Label -> CheckboxStyle.Label((data as RadioOptionGroupDataWithLabel).labelText)
        is Vertical.LeftAligned -> CheckboxStyle.LeftAligned
      }
      Checkbox(
        data = data.radioOptionData,
        checkboxStyle = checkboxStyle,
        lockedState = groupLockedState,
        checkboxSize = checkboxGroupSize.toOptionSize(),
        onClick = {
          onOptionClick(data.radioOptionData.id)
        },
      )
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun VerticalCheckboxGroupWithLabel(
  checkboxGroupStyle: VerticalWithGroupLabel<*>,
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
        style = checkboxGroupSize.getLabelTextStyle(),
        color = labelTextColor,
      )
      Column {
        checkboxGroupStyle.dataList.forEachIndexed { index, data ->
          val interactionSource = remember { MutableInteractionSource() }
          val checkboxStyle = when (checkboxGroupStyle) {
            is VerticalWithGroupLabel.Default -> CheckboxStyle.Default
            is VerticalWithGroupLabel.Icon -> CheckboxStyle.Icon(
              (data as RadioOptionGroupDataWithIcon).iconResource,
            )

            is VerticalWithGroupLabel.Label -> CheckboxStyle.Label(
              (data as RadioOptionGroupDataWithLabel).labelText,
            )

            is VerticalWithGroupLabel.LeftAligned -> CheckboxStyle.LeftAligned
          }
          Checkbox(
            data = data.radioOptionData,
            checkboxStyle = checkboxStyle,
            lockedState = groupLockedState,
            checkboxSize = checkboxGroupSize.toOptionSize(),
            interactionSource = interactionSource,
            modifier = Modifier
              .clickable(
                enabled = calculateLockedStateForItemInGroup(data.radioOptionData, groupLockedState) == NotLocked,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = ripple(
                  bounded = true,
                ),
                onClick = {
                  onOptionClick(data.radioOptionData.id)
                },
              )
              .then(
                if (index != 0) {
                  Modifier.horizontalDivider(DividerPosition.Top)
                } else {
                  Modifier
                },
              ),
          )
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
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel<*>) {
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
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel<*>) {
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
      bottom = if (checkboxGroupStyle !is VerticalWithGroupLabel<*>) {
        SmallSizeCheckboxGroupTokens.verticalPadding()
          .calculateBottomPadding()
      } else {
        0.dp
      },
    )
  }
  return when (checkboxGroupStyle) {
    is Vertical<*> -> PaddingValues()
    is VerticalWithGroupLabel<*> -> paddingValuesForLabel
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

@Composable
private fun CheckboxGroupSize.getLabelTextStyle(): TextStyle {
  return when (this) {
    Large -> LargeSizeCheckboxTokens.LabelTextFont.value
    Medium -> MediumSizeCheckboxTokens.LabelTextFont.value
    Small -> SmallSizeCheckboxTokens.LabelTextFont.value
  }
}

object CheckboxGroupDefaults {
  internal val checkboxGroupSize: CheckboxGroupSize = Large

  sealed interface CheckboxGroupStyle {
    sealed interface Vertical<T : RadioOptionGroupData> : CheckboxGroupStyle {
      val dataList: List<T>

      data class Default(override val dataList: List<RadioOptionGroupDataSimple>) : Vertical<RadioOptionGroupDataSimple>

      data class Label(override val dataList: List<RadioOptionGroupDataWithLabel>) :
        Vertical<RadioOptionGroupDataWithLabel>

      data class Icon(override val dataList: List<RadioOptionGroupDataWithIcon>) :
        Vertical<RadioOptionGroupDataWithIcon>

      data class LeftAligned(override val dataList: List<RadioOptionGroupDataSimple>) :
        Vertical<RadioOptionGroupDataSimple>
    }

    sealed interface VerticalWithGroupLabel<T : RadioOptionGroupData> : CheckboxGroupStyle {
      val dataList: List<T>
      val groupLabelText: String

      data class Default(
        override val groupLabelText: String,
        override val dataList: List<RadioOptionGroupDataSimple>,
      ) : VerticalWithGroupLabel<RadioOptionGroupDataSimple>

      data class Label(
        override val groupLabelText: String,
        override val dataList: List<RadioOptionGroupDataWithLabel>,
      ) : VerticalWithGroupLabel<RadioOptionGroupDataWithLabel>

      data class Icon(
        override val groupLabelText: String,
        override val dataList: List<RadioOptionGroupDataWithIcon>,
      ) : VerticalWithGroupLabel<RadioOptionGroupDataWithIcon>

      data class LeftAligned(
        override val groupLabelText: String,
        override val dataList: List<RadioOptionGroupDataSimple>,
      ) : VerticalWithGroupLabel<RadioOptionGroupDataSimple>
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
          onOptionClick = {},
        )
      }
    }
  }
}

internal class CheckboxGroupStyleProvider :
  CollectionPreviewParameterProvider<CheckboxGroupStyle>(
    listOf(
      Vertical.Default(previewListOfDataSimple),
      Vertical.Icon(previewListOfDataWithIcon),
      Vertical.Label(previewListOfDataWithLabel),
      Vertical.LeftAligned(previewListOfDataSimple),
      VerticalWithGroupLabel.Default("Group label", previewListOfDataSimple),
      VerticalWithGroupLabel.Icon("Group label", previewListOfDataWithIcon),
      VerticalWithGroupLabel.Label("Group label", previewListOfDataWithLabel),
      VerticalWithGroupLabel.LeftAligned("Group label", previewListOfDataSimple),
    ),
  )
