package com.hedvig.android.sample.design.showcase.bottomSheet

import android.R.attr.enabled
import android.R.attr.text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import hedvig.resources.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowcaseBottomSheet() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      val bottomSheetWithListState = rememberHedvigBottomSheetState<Unit>()
      val bottomSheetWithEditTextState = rememberHedvigBottomSheetState<Unit>()
      val bottomSheetWithLongListState = rememberHedvigBottomSheetState<Unit>()
      BottomSheetWithList(bottomSheetWithListState)
      BottomSheetWithEditText(bottomSheetWithEditTextState)
      BottomSheetWithLongList(bottomSheetWithLongListState)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(WindowInsets.safeDrawing.asPaddingValues())
          .verticalScroll(rememberScrollState()),
      ) {
        Spacer(Modifier.height(64.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore uis aute.",
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(40.dp))
        HedvigButton(
          enabled = true,
          onClick = bottomSheetWithListState::show,
        ) { HedvigText("Open sheet with List") }
        Spacer(Modifier.height(40.dp))
        HedvigButton(
          enabled = true,
          onClick = bottomSheetWithLongListState::show,
        ) { HedvigText("Open sheet with long list") }
        Spacer(Modifier.height(40.dp))
        HedvigButton(
          enabled = true,
          onClick = bottomSheetWithEditTextState::show,
        ) { HedvigText("Open sheet with edit text") }
        Spacer(Modifier.height(40.dp))
      }
    }
  }
}

@Composable
private fun BottomSheetWithList(sheetState: HedvigBottomSheetState<Unit>) {
  val chosenOption = remember { mutableStateOf<Int?>(null) }
  val listOfOptions = List(6) { index ->
    RadioOptionGroupData.RadioOptionGroupDataSimple(
      RadioOptionData(
        id = index.toString(),
        optionText = "Option $index",
        chosenState = if (index == chosenOption.value) Chosen else NotChosen,
      ),
    )
  }
  HedvigBottomSheet(sheetState) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(40.dp))
      RadioGroup(
        radioGroupStyle = RadioGroupStyle.Vertical.Default(listOfOptions),
        onOptionClick = { chosenOption.value = it.toInt() },
      )
      Spacer(Modifier.height(40.dp))
      HedvigButton(
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        onClick = sheetState::dismiss,
      ) { HedvigText("Save") }
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        onClick = sheetState::dismiss,
        text = stringResource(id = R.string.general_close_button),
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun BottomSheetWithLongList(sheetState: HedvigBottomSheetState<Unit>) {
  val chosenOption = remember { mutableStateOf<Int?>(null) }
  val listOfOptions = List(20) { index ->
    RadioOptionGroupData.RadioOptionGroupDataSimple(
      RadioOptionData(
        id = index.toString(),
        optionText = "Option $index",
        chosenState = if (index == chosenOption.value) Chosen else NotChosen,
      ),
    )
  }
  HedvigBottomSheet(sheetState) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(40.dp))
      RadioGroup(
        radioGroupStyle = RadioGroupStyle.Vertical.Default(listOfOptions),
        onOptionClick = { chosenOption.value = it.toInt() },
      )
      Spacer(Modifier.height(40.dp))
      HedvigButton(
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        onClick = sheetState::dismiss,
      ) { HedvigText("Save") }
      HedvigButton(
        onClick = sheetState::dismiss,
        text = stringResource(id = R.string.general_close_button),
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun BottomSheetWithEditText(sheetState: HedvigBottomSheetState<Unit>) {
  val textFieldValue = remember { mutableStateOf("") }
  HedvigBottomSheet(sheetState) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(40.dp))
      HedvigTextField(
        text = textFieldValue.value,
        onValueChange = {
          textFieldValue.value = it
        },
        labelText = "Your name",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      )
      Spacer(Modifier.height(40.dp))
      HedvigButton(
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        onClick = sheetState::dismiss,
      ) { HedvigText("Save") }
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        onClick = sheetState::dismiss,
        text = stringResource(id = R.string.general_close_button),
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}
