package com.hedvig.android.sample.design.showcase.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowcaseBottomSheet() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      val isBottomSheetWithListVisible = remember { mutableStateOf(false) }
      val isBottomSheetWithEditTextVisible = remember { mutableStateOf(false) }
      val isBottomSheetWithLongListVisible = remember { mutableStateOf(false) }
      BottomSheetWithList(isBottomSheetWithListVisible.value) { isBottomSheetWithListVisible.value = it }
      BottomSheetWithEditText(isBottomSheetWithEditTextVisible.value) { isBottomSheetWithEditTextVisible.value = it }
      BottomSheetWithLongList(isBottomSheetWithLongListVisible.value) { isBottomSheetWithLongListVisible.value = it }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(64.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore uis aute.",
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(40.dp))
        HedvigButton(enabled = true, onClick = {
          isBottomSheetWithListVisible.value = true
        }) { HedvigText("Open sheet with List") }
        Spacer(Modifier.height(40.dp))
        HedvigButton(enabled = true, onClick = {
          isBottomSheetWithLongListVisible.value = true
        }) { HedvigText("Open sheet with long list") }
        Spacer(Modifier.height(40.dp))
        HedvigButton(enabled = true, onClick = {
          isBottomSheetWithEditTextVisible.value = true
        }) { HedvigText("Open sheet with edit text") }
        Spacer(Modifier.height(40.dp))
      }
    }
  }
}

@Composable
private fun BottomSheetWithList(isBottomSheetVisible: Boolean, onVisibleChange: (Boolean) -> Unit) {
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
  HedvigBottomSheet(
    isVisible = isBottomSheetVisible,
    onVisibleChange = onVisibleChange,
    bottomButtonText = "Close",
  ) {
    Column(
      Modifier
        .fillMaxWidth(),
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
        onClick = {
          onVisibleChange(false)
        },
      ) { HedvigText("Save") }
      Spacer(Modifier.height(8.dp))
    }
  }
}

@Composable
private fun BottomSheetWithLongList(isBottomSheetVisible: Boolean, onVisibleChange: (Boolean) -> Unit) {
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
  HedvigBottomSheet(
    isVisible = isBottomSheetVisible,
    onVisibleChange = onVisibleChange,
    bottomButtonText = "Close",
  ) {
    Column(
      Modifier
        .fillMaxWidth(),
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
        onClick = {
          onVisibleChange(false)
        },
      ) { HedvigText("Save") }
      Spacer(Modifier.height(8.dp))
    }
  }
}

@Composable
private fun BottomSheetWithEditText(isBottomSheetVisible: Boolean, onVisibleChange: (Boolean) -> Unit) {
  val textFieldValue = remember { mutableStateOf("") }
  HedvigBottomSheet(
    isVisible = isBottomSheetVisible,
    onVisibleChange = onVisibleChange,
    bottomButtonText = "Close",
  ) {
    Column(
      Modifier
        .fillMaxWidth(),
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
        onClick = {
          onVisibleChange(false)
        },
      ) { HedvigText("Save") }
      Spacer(Modifier.height(8.dp))
    }
  }
}
