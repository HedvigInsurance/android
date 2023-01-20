package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.odyssey.input.InputViewModel
import kotlinx.coroutines.launch

@Composable
fun Location(viewModel: InputViewModel) {
  val coroutineScope = rememberCoroutineScope()
  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Text("Location", color = MaterialTheme.colors.onPrimary, fontSize = 40.sp)
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
}
