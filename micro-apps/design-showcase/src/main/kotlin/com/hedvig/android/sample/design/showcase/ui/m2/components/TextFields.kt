package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun M2TextFields() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Text Fields",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    Column {
      val text = remember { mutableStateOf("") }
      OutlinedTextField(
        value = text.value,
        onValueChange = { text.value = it },
        placeholder = { Text("Please type a text") },
      )
      Spacer(Modifier.size(16.dp))
      Column {
        OutlinedTextField(
          value = text.value,
          onValueChange = { text.value = it },
          placeholder = { Text("Please type a text") },
          isError = true,
        )
        TextFieldErrorMessage()
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TextFieldErrorMessage() {
  val startPadding = TextFieldDefaults.textFieldWithoutLabelPadding()
    .calculateStartPadding(layoutDirection = LocalLayoutDirection.current)

  Text(
    modifier = Modifier
      .padding(
        top = 4.dp,
        start = startPadding,
      ),
    text = "Something went wrong",
    style = MaterialTheme.typography.subtitle2,
    color = MaterialTheme.colors.error,
  )
}
