package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2Switch() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Switch",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    Row {
      var checked by remember { mutableStateOf(false) }
      Switch(checked = checked, onCheckedChange = { checked = !checked })
      Spacer(modifier = Modifier.size(16.dp))
      Switch(checked = !checked, onCheckedChange = { checked = !checked })
    }
  }
}
