package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
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
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle.VerticalWithLabel
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.Chosen
import com.hedvig.android.design.system.hedvig.RadioOptionChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Default
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.LargeSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.MediumSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.SmallSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens

@Composable
fun RadioGroup(
  data: List<RadioOptionData>,
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
    )

    is HorizontalWithLabel -> HorizontalRadioGroupWithLabel(
      data = data,
      groupLabelText = radioGroupStyle.groupLabelText,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      contentPaddingValues = contentPadding,
    )

    is Vertical -> VerticalRadioGroup(
      data = data,
      optionStyle = radioGroupStyle.optionStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
    )

    is VerticalWithLabel -> VerticalRadioGroupWithLabel(
      data = data,
      optionStyle = radioGroupStyle.optionStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      groupLabelText = radioGroupStyle.groupLabelText,
      contentPaddingValues = contentPadding,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HorizontalRadioGroup(
  data: List<RadioOptionData>,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier,
    maxItemsInEachRow = 2,
  ) {
    for (i in data) {
      val itemPadding = if (data.indexOf(i) % 2 != 0) {
        PaddingValues(bottom = 4.dp)
      } else {
        PaddingValues(bottom = 4.dp, end = 4.dp)
      }
      if (data.indexOf(i) % 2 == 0 && data.indexOf(i) == data.lastIndex) {
        RadioOption(
          data = i,
          radioOptionStyle = LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
          modifier = Modifier
            .weight(1f)
            .width(Min)
            .padding(itemPadding),
          // so with this implementation we make the last odd chip take half of the row,
          // but the downside is that the content of the last chip gets clipped if it's too long.
          // the other chips' content is not clipped
        )
        Spacer(Modifier.weight(1f))
      } else {
        RadioOption(
          data = i,
          radioOptionStyle = LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
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
        style = MediumSizeRadioOptionTokens.LabelTextFont.value, // same for all sizes in figma TODO
        color = labelTextColor,
      )
      Spacer(Modifier.height(16.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 2,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      ) {
        for (i in data) {
          val optionTextColor = radioOptionColors.optionTextColor(i.lockedState)
          val itemModifier = if (data.size == 2) {
            Modifier.width(Min)
          } else {
            Modifier
              .weight(1f)
              .width(Min)
          }
          HorizontalWithLabelRadioOption(
            i = i,
            shape = shape,
            enabled = groupLockedState == NotLocked && i.lockedState == NotLocked, // TODO,
            style = radioGroupSize.toOptionSize().size(LeftAligned).optionTextStyle,
            textColor = optionTextColor,
            modifier = itemModifier,
          )
        }
      }
    }
  }
}

@Composable
private fun HorizontalWithLabelRadioOption(
  i: RadioOptionData,
  shape: Shape,
  enabled: Boolean,
  style: TextStyle,
  textColor: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(shape)
      .clickable(
        enabled = enabled,
        role = Role.RadioButton,
      ) {
        if (i.lockedState != Locked) { // TODO
          i.onClick()
        }
      },
  ) {
    SelectIndicationCircle(
      i.chosenState,
      i.lockedState, // TODO
    )
    Spacer(Modifier.width(8.dp))
    HedvigText(
      i.optionText,
      style = style,
      color = textColor,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.width(16.dp))
  }
}

@Composable
private fun VerticalRadioGroup(
  data: List<RadioOptionData>,
  optionStyle: RadioOptionStyle,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    for (radioOptionData in data) {
      RadioOption(
        data = radioOptionData,
        radioOptionStyle = optionStyle,
        groupLockedState = groupLockedState,
        radioOptionSize = radioGroupSize.toOptionSize(),
      )
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun VerticalRadioGroupWithLabel(
  data: List<RadioOptionData>,
  groupLabelText: String,
  optionStyle: RadioOptionStyle,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  contentPaddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = radioGroupSize.toOptionSize().size(LeftAligned).shape,
    color = radioOptionColors.containerColor,
  ) {
    Column(
      modifier,
    ) {
      val labelTextColor = radioOptionColors.labelTextColor(groupLockedState)
      HedvigText(
        modifier = Modifier.padding(contentPaddingValues),
        text = groupLabelText,
        style = MediumSizeRadioOptionTokens.LabelTextFont.value, // same for all sizes in figma
        color = labelTextColor,
      )
      Column(modifier) {
        for (radioOptionData in data) {
          RadioOption(
            data = radioOptionData,
            radioOptionStyle = optionStyle,
            groupLockedState = groupLockedState,
            radioOptionSize = radioGroupSize.toOptionSize(),
          )
          HorizontalDivider()
        }
      }
    }
  }
}

private fun calculateContentPadding(radioGroupStyle: RadioGroupStyle, radioGroupSize: RadioGroupSize): PaddingValues {
  val paddingValuesForLabel = when (radioGroupSize) {
    Large -> PaddingValues(
      start = LargeSizeRadioGroupTokens.HorizontalPadding,
      end = LargeSizeRadioGroupTokens.HorizontalPadding,
      top = LargeSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = if (radioGroupStyle !is VerticalWithLabel) {
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
      bottom = if (radioGroupStyle !is VerticalWithLabel) {
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
      bottom = if (radioGroupStyle !is VerticalWithLabel) {
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
    is VerticalWithLabel -> paddingValuesForLabel
  }
}

private fun RadioGroupSize.toOptionSize(): RadioOptionDefaults.RadioOptionSize {
  return when (this) {
    Large -> RadioOptionDefaults.RadioOptionSize.Large
    Medium -> RadioOptionDefaults.RadioOptionSize.Medium
    Small -> RadioOptionDefaults.RadioOptionSize.Small
  }
}

object RadioGroupDefaults {
  internal val radioGroupStyle: RadioGroupStyle = Vertical(Default)
  internal val radioGroupSize: RadioGroupSize = Large

  sealed interface RadioGroupStyle {
    data object Horizontal : RadioGroupStyle

    data class HorizontalWithLabel(
      val groupLabelText: String,
    ) : RadioGroupStyle

    data class Vertical(
      val optionStyle: RadioOptionStyle,
    ) : RadioGroupStyle

    data class VerticalWithLabel(val optionStyle: RadioOptionStyle, val groupLabelText: String) : RadioGroupStyle
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
  @PreviewParameter(HorizontalGroupParametersProvider::class) size: RadioGroupSize,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(Modifier.padding(horizontal = 16.dp)) {
        HedvigText("Horizontal")
        HorizontalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier,
          data = listOf(
            RadioOptionData(
              optionText = "Yes",
              onClick = {},
              chosenState = Chosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(6.dp))
        HorizontalRadioGroup(
          groupLockedState = Locked,
          radioGroupSize = size,
          modifier = Modifier,
          data = listOf(
            RadioOptionData(
              optionText = "Yes",
              onClick = {},
              chosenState = Chosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
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
          data = listOf(
            RadioOptionData(
              optionText = "Yes",
              onClick = {},
              chosenState = Chosen,
            ),
            RadioOptionData(
              optionText = "Non",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Maybe",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Not sure",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Probably",
              onClick = {},
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
          contentPaddingValues = calculateContentPadding(HorizontalWithLabel("Label"), size),
          data = listOf(
            RadioOptionData(
              optionText = "Yes",
              onClick = {},
              chosenState = Chosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
            ),
          ),
        )
        Spacer(Modifier.height(4.dp))
        Row {
          HorizontalRadioGroupWithLabel(
            groupLockedState = NotLocked,
            radioGroupSize = size,
            modifier = Modifier.fillMaxWidth(),
            groupLabelText = "Label",
            contentPaddingValues = calculateContentPadding(HorizontalWithLabel("Label"), size),
            data = listOf(
              RadioOptionData(
                optionText = "Yes",
                onClick = {},
                chosenState = Chosen,
              ),
              RadioOptionData(
                optionText = "No",
                onClick = {},
                chosenState = NotChosen,
              ),
              RadioOptionData(
                optionText = "Maybet",
                onClick = {},
                chosenState = NotChosen,
              ),
              RadioOptionData(
                optionText = "Not sure",
                onClick = {},
                chosenState = NotChosen,
              ),
              RadioOptionData(
                optionText = "Perhaps",
                onClick = {},
                chosenState = NotChosen,
              ),
              RadioOptionData(
                optionText = "Very unlikely",
                onClick = {},
                chosenState = NotChosen,
              ),
            ),
          )
        }
        HedvigText("Vertical")
        VerticalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier.fillMaxWidth(),
          optionStyle = Default,
          data = listOf(
            RadioOptionData(
              optionText = "Yes",
              onClick = {},
              chosenState = Chosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = Locked,
            ),
            RadioOptionData(
              optionText = "Maybe",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Not sure",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Perhaps",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "Very unlikely",
              onClick = {},
              chosenState = NotChosen,
            ),
          ),
        )
      }
    }
  }
}

@Preview
@Composable
fun VerticalGroupsPreview(
  @PreviewParameter(HorizontalGroupParametersProvider::class) size: RadioGroupSize,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(Modifier.padding(horizontal = 16.dp)) {
        HedvigText("Vertical")
        VerticalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          modifier = Modifier.fillMaxWidth(),
          optionStyle = Default,
          data = listOf(
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
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
          optionStyle = Default,
          groupLabelText = "Label",
          contentPaddingValues = calculateContentPadding(
            VerticalWithLabel(
              groupLabelText = "Label",
              optionStyle = RadioOptionStyle.Icon(IconResource.Vector(HedvigIcons.Play)),
            ),
            size,
          ),
          data = listOf(
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
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
  @PreviewParameter(RadiOptionStyleParametersProvider::class) style: RadioOptionStyle,
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
          optionStyle = style,
          groupLabelText = "Label",
          contentPaddingValues = calculateContentPadding(
            VerticalWithLabel(
              groupLabelText = "Label",
              optionStyle = RadioOptionStyle.Icon(IconResource.Vector(HedvigIcons.Play)),
            ),
            Small,
          ),
          data = listOf(
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = Chosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
            RadioOptionData(
              optionText = "Vertical option",
              onClick = {},
              chosenState = NotChosen,
            ),
            RadioOptionData(
              optionText = "No",
              onClick = {},
              chosenState = NotChosen,
              lockedState = NotLocked,
            ),
          ),
        )
      }
    }
  }
}

private class HorizontalGroupParametersProvider :
  CollectionPreviewParameterProvider<RadioGroupSize>(
    listOf(
      Large,
      Medium,
      Small,
    ),
  )

private class RadiOptionStyleParametersProvider :
  CollectionPreviewParameterProvider<RadioOptionStyle>(
    listOf(
      Default,
      Label("Label"),
      RadioOptionStyle.Icon(IconResource.Painter(hedvig.resources.R.drawable.pillow_hedvig)),
      RadioOptionStyle.Icon(IconResource.Vector(HedvigIcons.FlagSweden)),
      LeftAligned,
    ),
  )
