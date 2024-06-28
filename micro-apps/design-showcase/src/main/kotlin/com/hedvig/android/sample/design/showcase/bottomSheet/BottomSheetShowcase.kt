package com.hedvig.android.sample.design.showcase.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetType.FullScreenWithManyTextInputs
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetType.SimpleHalfScreenSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowcaseBottomSheet() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundWhite,
      modifier = Modifier.fillMaxSize(),
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isBottomSheetVisible = remember { mutableStateOf(false) }
        val isBottomSheetVisible2 = remember { mutableStateOf(false) }
        val isBottomSheetVisible3 = remember { mutableStateOf(false) }
        val textFieldValue = remember { mutableStateOf("") }
        val textFieldValue2 = remember { mutableStateOf("") }
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Name: ${textFieldValue.value} \nphone: ${textFieldValue2.value}",
          modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(40.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
          HedvigButton(
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = { isBottomSheetVisible.value = true },
          ) { HedvigText("Open fullscreen sheet with scroll and text input", textAlign = TextAlign.Center) }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = { isBottomSheetVisible2.value = true },
          ) { HedvigText("Open simple halfscreen sheet", textAlign = TextAlign.Center) }
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = { isBottomSheetVisible3.value = true },
          ) { HedvigText("Open halfscreen sheet with text input", textAlign = TextAlign.Center) }
          Spacer(Modifier.height(40.dp))
        }
        if (isBottomSheetVisible3.value) {
          HalfScreenBottomSheetWithInput(
            onVisibleChange = { bool ->
              isBottomSheetVisible3.value = bool
            },
          )
        }
        if (isBottomSheetVisible2.value) {
          SimpleBottomSheet(
            onVisibleChange = { bool ->
              isBottomSheetVisible2.value = bool
            },
          )
        }
        if (isBottomSheetVisible.value) {
          LongBottomSheetWithManyTextInputs(
            textFieldValue = textFieldValue.value,
            textFieldValue2 = textFieldValue2.value,
            onVisibleChange = { bool ->
              isBottomSheetVisible.value = bool
            },
            onValueChange1 = {
              textFieldValue.value = it
            },
            onValueChange2 = {
              textFieldValue2.value = it
            },
            onCancelButtonCLick = {
              isBottomSheetVisible.value = false
              textFieldValue.value = ""
            },
            onSaveButtonCLick = {
              isBottomSheetVisible.value = false
            },
          )
        }
      }
    }
  }
}

@Composable
private fun LongBottomSheetWithManyTextInputs(
  textFieldValue: String,
  textFieldValue2: String,
  onVisibleChange: (Boolean) -> Unit,
  onValueChange1: (String) -> Unit,
  onValueChange2: (String) -> Unit,
  onSaveButtonCLick: () -> Unit,
  onCancelButtonCLick: () -> Unit,
) {
  HedvigBottomSheet(
    onVisibleChange = onVisibleChange,
    allowNestedScroll = false,
    sheetType = FullScreenWithManyTextInputs,
    content = {
      Column(
        Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        )
        Spacer(Modifier.height(40.dp))
        HedvigTextField(
          text = textFieldValue,
          textFieldSize = Medium,
          labelText = "Name",
          onValueChange = onValueChange1,
        )
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        )
        Spacer(Modifier.height(40.dp))
        HedvigTextField(
          text = textFieldValue2,
          textFieldSize = Medium,
          labelText = "Phone",
          onValueChange = onValueChange2,
        )
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        )
        Spacer(Modifier.height(40.dp))
        HedvigButton(
          enabled = textFieldValue != "",
          onClick = onSaveButtonCLick,
        ) { HedvigText("Save") }
        Spacer(Modifier.height(40.dp))
        HedvigButton(
          enabled = true,
          onClick = onCancelButtonCLick,
        ) { HedvigText("Remove value") }
        Spacer(Modifier.height(16.dp))
      }
    },
  )
}

@Composable
private fun SimpleBottomSheet(onVisibleChange: (Boolean) -> Unit) {
  HedvigBottomSheet(
    onVisibleChange = onVisibleChange,
    allowNestedScroll = true,
    sheetType = SimpleHalfScreenSheet,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      HedvigText(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(onClick = {}, enabled = true, content = { HedvigText("Button") })
    }
  }
}

@Composable
private fun HalfScreenBottomSheetWithInput(onVisibleChange: (Boolean) -> Unit) {
  HedvigBottomSheet(
    onVisibleChange = onVisibleChange,
    allowNestedScroll = true,
    sheetType = SimpleHalfScreenSheet,
  ) {
    var textFieldValue by remember { mutableStateOf("placeholder") }
    Column {
      HedvigText(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
      )
      Spacer(Modifier.height(16.dp))
      HedvigTextField(
        text = textFieldValue,
        textFieldSize = Medium,
        labelText = "Phone",
        modifier = Modifier.padding(horizontal = 16.dp),
        onValueChange = { textFieldValue = it },
      )
    }
  }
}
