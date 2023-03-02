package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2Chips() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Chips",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    var selectedId by remember { mutableStateOf(1) }
    FilterChip(
      label = { Text("Android") },
      onClick = { selectedId = 1 },
      selected = selectedId == 1,
    )
    Spacer(modifier = Modifier.size(4.dp))
    FilterChip(
      label = { Text("Material") },
      onClick = { selectedId = 2 },
      selected = selectedId == 2,
    )
    Spacer(modifier = Modifier.size(4.dp))
    FilterChip(
      label = { Text("Compose") },
      onClick = { selectedId = 3 },
      selected = selectedId == 3,
    )
  }
}
