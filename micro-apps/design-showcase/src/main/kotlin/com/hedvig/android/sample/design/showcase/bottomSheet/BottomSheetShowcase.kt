package com.hedvig.android.sample.design.showcase.bottomSheet

import HedvigBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        var isBottomSheetVisible by remember { mutableStateOf(false) }
        var textFieldValue by remember { mutableStateOf("enter your name") }
        val isImeVisible = WindowInsets.isImeVisible
        Spacer(Modifier.height(40.dp))
        HedvigText(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..",
          Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(40.dp))
        HedvigButton(enabled = true, onClick = { isBottomSheetVisible = true }) { HedvigText("Open sheet") }
        Spacer(Modifier.height(40.dp))
        HedvigBottomSheet(
          isVisible = isBottomSheetVisible,
          onVisibleChange = { isBottomSheetVisible = it },
          onSystemBack = null,
          sheetPadding = if (isImeVisible) {
            WindowInsets.ime.asPaddingValues()
          } else {
            WindowInsets.safeDrawing
              .only(WindowInsetsSides.Bottom).asPaddingValues()
          },
          content = {
            Column(
              Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              HedvigText(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
              )
              Spacer(Modifier.height(40.dp))
              BasicTextField(
                value = textFieldValue,
                modifier = Modifier.background(Color.Red),
                onValueChange = {
                  textFieldValue = it
                },
              )
              Spacer(Modifier.height(40.dp))
              HedvigButton(enabled = textFieldValue != "", onClick = {
                isBottomSheetVisible = false
              }) { HedvigText("Save") }
              Spacer(Modifier.height(40.dp))
              HedvigButton(enabled = true, onClick = {
                isBottomSheetVisible = false
                textFieldValue = ""
              }) { HedvigText("Cancel") }
            }
          },
        )
      }
    }
  }
}
