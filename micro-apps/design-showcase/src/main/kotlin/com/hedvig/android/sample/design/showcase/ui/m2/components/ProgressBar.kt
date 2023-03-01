package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2ProgressBar() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Progress bar ",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    CircularProgressIndicator()
  }
}
