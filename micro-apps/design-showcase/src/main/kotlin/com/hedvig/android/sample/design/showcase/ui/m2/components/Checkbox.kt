package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2Checkbox() {
  var checked by remember { mutableStateOf(true) }
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Checkbox",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    Checkbox(
      checked = checked,
      onCheckedChange = { checked = it },
    )
    Spacer(Modifier.size(8.dp))
    Checkbox(
      checked = !checked,
      onCheckedChange = { checked = !it },
    )
  }
}
