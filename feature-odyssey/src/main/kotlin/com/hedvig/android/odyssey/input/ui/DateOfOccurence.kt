package com.hedvig.android.odyssey.input.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.hedvig.android.odyssey.input.InputViewModel

@Composable
internal fun DateOfOccurrence(viewModel: InputViewModel) {
  Column {
    Text("Date", color = MaterialTheme.colors.onPrimary, fontSize = 40.sp)
    Button(
      onClick = {
        viewModel.onNext()
      },
    ) {
      Text(text = "Next")
    }
  }
}
