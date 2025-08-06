package com.hedvig.android.sample.design.showcase.materialBottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show

@Composable
internal fun MaterialExperiment() {
  val bottomSheetOneState = rememberHedvigBottomSheetState<Unit>()
  LongSheetWithEditTextDefaultDescriptions(bottomSheetOneState)
  val bottomSheetTwoState = rememberHedvigBottomSheetState<Unit>()
  LongSheetWithTextAndButtonsCustomDescription(bottomSheetTwoState)
  Column(
    Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceAround,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    HedvigButton(
      onClick = bottomSheetOneState::show,
      enabled = true,
      text = "Open bottom sheet with edit text",
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigButton(
      onClick = bottomSheetOneState::show,
      enabled = true,
      text = "Open bottom sheet with text and buttons",
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigText(
      "Quite long text that we don't want to be read by TalkBack " +
        "if the bottom sheet is expanded.",
    )
  }
}

@Composable
private fun LongSheetWithEditTextDefaultDescriptions(bottomSheetOneState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(bottomSheetOneState) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText("Yellow daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = bottomSheetOneState::dismiss,
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      EditTextForSheet()
      HedvigText(LOREM_IPSUM)
    }
  }
}

@Composable
private fun LongSheetWithTextAndButtonsCustomDescription(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(
    hedvigBottomSheetState = sheetState,
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText("Blue daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText("Space daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText("Alien daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HedvigText(LOREM_IPSUM)
      EditTextForSheet()
    }
  }
}

@Composable
private fun EditTextForSheet() {
  var value by remember { mutableStateOf<String?>(null) }
  HedvigTextField(
    text = value ?: "",
    onValueChange = {
      value = it
    },
    labelText = "Label",
    textFieldSize = TextFieldSize.Medium,
  )
}

private val LOREM_IPSUM = LoremIpsum(200).values.first()
