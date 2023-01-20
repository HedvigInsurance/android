package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import com.hedvig.android.odyssey.input.InputViewModel
import kotlinx.coroutines.launch

@Composable
fun PhoneNumber(viewModel: InputViewModel) {
  val coroutineScope = rememberCoroutineScope()
  Column {
    Text("Phone", color = MaterialTheme.colors.onPrimary, fontSize = 40.sp)
    Button(
      onClick = {
        coroutineScope.launch {
          viewModel.onNext()
        }
      },
    ) {
      Text(text = "Next")
    }
  }
}
