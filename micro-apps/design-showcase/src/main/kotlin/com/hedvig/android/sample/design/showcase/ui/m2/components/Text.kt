package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

@Composable
internal fun M2OnSurfaceText(
  text: String,
  style: TextStyle,
) {
  Text(
    color = MaterialTheme.colors.onSurface,
    text = text,
    style = style,
  )
}
