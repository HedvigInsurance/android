package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.ui.clearFocusOnTap

@Composable
internal fun HTextField() {
  Box(
    modifier = Modifier.fillMaxSize().clearFocusOnTap(),
    contentAlignment = BiasAlignment(0f, -0.2f),
  ) {
    Column {
      var text by remember { mutableStateOf("Error!") }
      var isError by remember { mutableStateOf(false) }
      var isEnabled by remember { mutableStateOf(true) }
      Row { Text("isError:$isError"); Checkbox(checked = isError, onCheckedChange = { isError = it }) }
      Row { Text("isEnabled:$isEnabled"); Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it }) }
      HedvigTextField(
        value = text,
        onValueChange = { text = it },
        errorText = if (isError) {
          "Ditt personnummer stämmer inte."
        } else {
          null
        },
        isError = isError,
        enabled = isEnabled,
      )
    }
  }
}
