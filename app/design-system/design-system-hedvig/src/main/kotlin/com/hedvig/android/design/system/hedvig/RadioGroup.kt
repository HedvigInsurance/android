package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize.Max
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection.Ltr
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
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.LeftAligned
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.LargeSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.MediumSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioGroupTokens.SmallSizeRadioGroupTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeRadioOptionTokens.MediumSizeRadioOptionTokens

@Composable
fun RadioGroup(
  data: List<RadioOptionData>,
  modifier: Modifier = Modifier,
  longListUseLazyScroll: Boolean = true,
  // not sure about this. Added this parameter here to have some flexibility when it comes to scroll call:
  // to avoid problems with nested scroll/infinity max height,
  // when the lazyColumn/Row may not even be needed since the list is very short. In that case we'll just use a simple
  // Column/Row here and leave handling the scroll to the parent Composable
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
      longListUseLazyScroll = longListUseLazyScroll,
    )

    is HorizontalWithLabel -> HorizontalRadioGroupWithLabel(
      data = data,
      groupLabelText = radioGroupStyle.groupLabelText,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      longListUseLazyScroll = longListUseLazyScroll,
      contentPaddingValues = contentPadding,
    )

    is Vertical -> VerticalRadioGroup(
      data = data,
      optionStyle = radioGroupStyle.optionStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      longListUseLazyScroll = longListUseLazyScroll,
    )

    is VerticalWithLabel -> VerticalRadioGroupWithLabel(
      data = data,
      optionStyle = radioGroupStyle.optionStyle,
      modifier = modifier,
      groupLockedState = groupLockedState,
      radioGroupSize = radioGroupSize,
      groupLabelText = radioGroupStyle.groupLabelText,
      longListUseLazyScroll = longListUseLazyScroll,
      contentPaddingValues = contentPadding,
    )
  }
}

@Composable
private fun HorizontalRadioGroup(
  data: List<RadioOptionData>,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  longListUseLazyScroll: Boolean,
  modifier: Modifier = Modifier,
) {
  if (longListUseLazyScroll) {
    LazyRow(modifier) {
      items(
        items = data,
        key = { it.optionText },
      ) { radioOptionData ->
        RadioOption(
          data = radioOptionData,
          radioOptionStyle = RadioOptionStyle.LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
          modifier = Modifier.width(intrinsicSize = Max),
        )
        Spacer(Modifier.width(4.dp))
      }
    }
  } else {
    Row(modifier) {
      for (i in data) {
        RadioOption(
          data = i,
          radioOptionStyle = RadioOptionStyle.LeftAligned,
          radioOptionSize = radioGroupSize.toOptionSize(),
          groupLockedState = groupLockedState,
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(4.dp))
      }
    }
  }
}

@Composable
private fun HorizontalRadioGroupWithLabel(
  data: List<RadioOptionData>,
  groupLabelText: String,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  longListUseLazyScroll: Boolean,
  contentPaddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = radioGroupSize.toOptionSize().size(LeftAligned).shape,
    color = radioOptionColors.containerColor,
  ) {
    Column(
      modifier.padding(contentPaddingValues),
    ) {
      val labelTextColor = radioOptionColors.labelTextColor(groupLockedState)
      HedvigText(
        text = groupLabelText,
        style = MediumSizeRadioOptionTokens.LabelTextFont.value, // same for all sizes in figma
        color = labelTextColor,
      )
      Spacer(Modifier.height(16.dp))
      if (longListUseLazyScroll) {
        LazyRow {
          items(
            items = data,
            key = { it.optionText },
          ) { radioOptionData ->
            val optionTextColor = radioOptionColors.optionTextColor(radioOptionData.lockedState)
            Row(
              Modifier.width(intrinsicSize = Max),
            ) {
              SelectIndicationCircle(
                radioOptionData.chosenState,
                radioOptionData.lockedState,
                Modifier
                  .clip(CircleShape)
                  .clickable(
                    enabled = groupLockedState == NotLocked && radioOptionData.lockedState == NotLocked,
                    role = Role.RadioButton,
                  ) {
                    if (radioOptionData.lockedState != Locked) {
                      radioOptionData.onClick()
                    }
                  },
              )
              Spacer(Modifier.width(8.dp))
              HedvigText(
                radioOptionData.optionText,
                style = radioGroupSize.toOptionSize().size(LeftAligned).optionTextStyle,
                color = optionTextColor,
                modifier = Modifier.weight(1f),
              )
              Spacer(Modifier.width(16.dp))
            }
          }
        }
      } else {
        Row {
          for (i in data) {
            val optionTextColor = radioOptionColors.optionTextColor(i.lockedState)
            Row(
              Modifier
                .sizeIn(maxHeight = 40.dp)
                .width(intrinsicSize = Max),
            ) {
              SelectIndicationCircle(
                i.chosenState,
                i.lockedState,
                Modifier
                  .clip(CircleShape)
                  .clickable(
                    enabled = groupLockedState == NotLocked && i.lockedState == NotLocked,
                    role = Role.RadioButton,
                  ) {
                    if (i.lockedState != Locked) {
                      i.onClick()
                    }
                  },
              )
              Spacer(Modifier.width(16.dp))
              HedvigText(
                i.optionText,
                style = radioGroupSize.toOptionSize().size(LeftAligned).optionTextStyle,
                color = optionTextColor,
              )
              Spacer(Modifier.width(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VerticalRadioGroup(
  data: List<RadioOptionData>,
  optionStyle: RadioOptionStyle,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  longListUseLazyScroll: Boolean,
  modifier: Modifier = Modifier,
) {
  if (longListUseLazyScroll) {
    LazyColumn(modifier) {
      items(
        items = data,
        key = { it.optionText },
      ) { radioOptionData ->
        RadioOption(
          data = radioOptionData,
          radioOptionStyle = optionStyle,
          groupLockedState = groupLockedState,
          radioOptionSize = radioGroupSize.toOptionSize(),
        )
        Spacer(Modifier.height(4.dp))
      }
    }
  } else {
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
}

@Composable
private fun VerticalRadioGroupWithLabel(
  data: List<RadioOptionData>,
  groupLabelText: String,
  optionStyle: RadioOptionStyle,
  groupLockedState: LockedState,
  radioGroupSize: RadioGroupSize,
  longListUseLazyScroll: Boolean,
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
        modifier = Modifier.padding(
          top = contentPaddingValues.calculateTopPadding(),
          start = contentPaddingValues.calculateStartPadding(Ltr),
          end = contentPaddingValues.calculateEndPadding(Ltr),
        ),
        text = groupLabelText,
        style = MediumSizeRadioOptionTokens.LabelTextFont.value, // same for all sizes in figma
        color = labelTextColor,
      )
      if (longListUseLazyScroll) {
        LazyColumn(modifier) {
          items(
            items = data,
            key = { it.optionText },
          ) { radioOptionData ->
            RadioOption(
              data = radioOptionData,
              radioOptionStyle = optionStyle,
              groupLockedState = groupLockedState,
              radioOptionSize = radioGroupSize.toOptionSize(),
            )
            HorizontalDivider()
          }
        }
      } else {
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
}

private fun calculateContentPadding(radioGroupStyle: RadioGroupStyle, radioGroupSize: RadioGroupSize): PaddingValues {
  val paddingValuesForLabel = when (radioGroupSize) {
    Large -> PaddingValues(
      start = LargeSizeRadioGroupTokens.HorizontalPadding,
      end = LargeSizeRadioGroupTokens.HorizontalPadding,
      top = LargeSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = LargeSizeRadioGroupTokens.verticalPadding().calculateBottomPadding(),
    )

    Medium -> PaddingValues(
      start = MediumSizeRadioGroupTokens.HorizontalPadding,
      end = MediumSizeRadioGroupTokens.HorizontalPadding,
      top = MediumSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = MediumSizeRadioGroupTokens.verticalPadding().calculateBottomPadding(),
    )

    Small -> PaddingValues(
      start = SmallSizeRadioGroupTokens.HorizontalPadding,
      end = SmallSizeRadioGroupTokens.HorizontalPadding,
      top = SmallSizeRadioGroupTokens.verticalPadding().calculateTopPadding(),
      bottom = SmallSizeRadioGroupTokens.verticalPadding().calculateBottomPadding(),
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
          longListUseLazyScroll = false,
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
          longListUseLazyScroll = false,
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
        HedvigText("Horizontal with longListUseLazyScroll = true")
        HorizontalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          longListUseLazyScroll = true,
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
          ),
        )
        Spacer(Modifier.height(16.dp))
        HedvigText("Horizontal with label")
        HorizontalRadioGroupWithLabel(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          longListUseLazyScroll = false,
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
          ),
        )
        Spacer(Modifier.height(4.dp))
        Row(
          Modifier.horizontalScroll(rememberScrollState()),
        ) {
          HorizontalRadioGroupWithLabel(
            groupLockedState = NotLocked,
            radioGroupSize = size,
            longListUseLazyScroll = false,
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
        HedvigText("Horizontal with label with longListUseLazyScroll")
        HorizontalRadioGroupWithLabel(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          longListUseLazyScroll = true,
          modifier = Modifier.fillMaxWidth(),
          groupLabelText = "Label",
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
          contentPaddingValues = calculateContentPadding(HorizontalWithLabel("Label"), size),
        )
        HedvigText("Vertical with longListUseLazyScroll")
        VerticalRadioGroup(
          groupLockedState = NotLocked,
          radioGroupSize = size,
          longListUseLazyScroll = true,
          modifier = Modifier.fillMaxWidth(),
          optionStyle = RadioOptionStyle.Default,
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
          longListUseLazyScroll = false,
          modifier = Modifier.fillMaxWidth(),
          optionStyle = RadioOptionStyle.Default,
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
          longListUseLazyScroll = false,
          modifier = Modifier.fillMaxWidth(),
          optionStyle = RadioOptionStyle.Default,
          groupLabelText = "Label",
          contentPaddingValues = calculateContentPadding(
            RadioGroupStyle.VerticalWithLabel(
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

private class HorizontalGroupParametersProvider :
  CollectionPreviewParameterProvider<RadioGroupSize>(
    listOf(
      Large,
      Medium,
      Small,
    ),
  )
