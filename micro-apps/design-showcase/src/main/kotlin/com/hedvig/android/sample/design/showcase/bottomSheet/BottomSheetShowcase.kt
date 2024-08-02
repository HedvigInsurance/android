package com.hedvig.android.sample.design.showcase.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowcaseBottomSheet() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val isBottomSheetVisible = remember { mutableStateOf(false) }

        val textFieldValue = remember { mutableStateOf("enter your name") }
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..",
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(40.dp))
        Spacer(Modifier.height(40.dp))
        HedvigButton(enabled = true, onClick = { isBottomSheetVisible.value = true }) { HedvigText("Open sheet") }
        Spacer(Modifier.height(40.dp))
        HedvigBottomSheet(
          isVisible = isBottomSheetVisible.value,
          onVisibleChange = { bool ->
            isBottomSheetVisible.value = bool
          },
          bottomButtonText = "Close",
        ) {
          Column(
            Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            HedvigText(
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            )
            Spacer(Modifier.height(40.dp))
            BasicTextField(
              value = textFieldValue.value,
              modifier = Modifier.background(Color.Red),
              onValueChange = {
                textFieldValue.value = it
              },
            )
            Spacer(Modifier.height(40.dp))
            HedvigButton(
              enabled = textFieldValue.value != "",
              modifier = Modifier.fillMaxWidth(),
              onClick = {
                isBottomSheetVisible.value = false
              },
            ) { HedvigText("Save") }
            Spacer(Modifier.height(8.dp))
          }
        }
      }
    }
  }
}
