package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2TopAppBars() {
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Top app bars",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Not Scrolled State",
      style = MaterialTheme.typography.subtitle2,
    )
    Spacer(Modifier.size(4.dp))
    TopAppBar(
      title = { Text("Small top app bar") },
      navigationIcon = { NavigationIcon() },
    )
    Spacer(Modifier.size(4.dp))
  }
}

@Composable
private fun NavigationIcon() {
  IconButton(onClick = {}) {
    Icon(
      imageVector = Icons.Default.ArrowBack,
      contentDescription = null,
    )
  }
}
