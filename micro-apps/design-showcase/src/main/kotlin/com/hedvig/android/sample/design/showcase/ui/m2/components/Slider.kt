package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2Slider() {
  var sliderPosition by remember { mutableStateOf(0.5f) }
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Slider",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    Slider(sliderPosition, { sliderPosition = it })
  }
}
