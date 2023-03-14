package com.hedvig.android.odyssey.step.dateofoccurrence

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
internal fun DateOfOccurrenceDestination() {
  DateOfOccurrenceScreen()
}

@Composable
private fun DateOfOccurrenceScreen() {
  DateOfOccurrence()
}

@Composable
private fun DateOfOccurrence() {
  Column {
    Text("Date", color = MaterialTheme.colors.onPrimary, fontSize = 40.sp)
    Button(
      onClick = {
        // todo this click
      },
    ) {
      Text(text = "Next")
    }
  }
}
