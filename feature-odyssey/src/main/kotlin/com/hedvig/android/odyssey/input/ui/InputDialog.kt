package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PriceInputDialog(
  title: String,
  message: MutableState<String>,
  openDialog: MutableState<Boolean>,
  editMessage: MutableState<String>,
  onPositiveButtonClicked: (String) -> Unit,
  onDismissRequest: () -> Unit,
) {
  Dialog(onDismissRequest = { onDismissRequest.invoke() }) {
    Surface(
      modifier = Modifier.width(300.dp),
      shape = RoundedCornerShape(10.dp),
    ) {
      Column(
        modifier = Modifier.padding(8.dp),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
        ) {
          Text(text = title)

          Spacer(modifier = Modifier.height(8.dp))

          TextField(
            value = editMessage.value,
            onValueChange = { editMessage.value = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.align(Alignment.End),
        ) {
          TextButton(
            onClick = {
              openDialog.value = false
            },
          ) {
            Text("Cancel")
          }

          Spacer(modifier = Modifier.width(8.dp))

          TextButton(
            onClick = {
              message.value = editMessage.value
              onPositiveButtonClicked(message.value)
              openDialog.value = false
            },
          ) {
            Text("OK")
          }
        }
      }

    }
  }
}
