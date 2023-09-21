package com.hedvig.android.sample.design.showcase.ui.colorscheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun HedvigColorScheme() {
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    ColorItem({ MaterialTheme.colorScheme.primary }, "primary")
    ColorItem({ MaterialTheme.colorScheme.onPrimary }, "onPrimary")
    ColorItem({ MaterialTheme.colorScheme.primaryContainer }, "primaryContainer")
    ColorItem({ MaterialTheme.colorScheme.onPrimaryContainer }, "onPrimaryContainer")
    ColorItem({ MaterialTheme.colorScheme.inversePrimary }, "inversePrimary")
    ColorItem({ MaterialTheme.colorScheme.secondary }, "secondary")
    ColorItem({ MaterialTheme.colorScheme.onSecondary }, "onSecondary")
    ColorItem({ MaterialTheme.colorScheme.secondaryContainer }, "secondaryContainer")
    ColorItem({ MaterialTheme.colorScheme.onSecondaryContainer }, "onSecondaryContainer")
    ColorItem({ MaterialTheme.colorScheme.tertiary }, "tertiary")
    ColorItem({ MaterialTheme.colorScheme.onTertiary }, "onTertiary")
    ColorItem({ MaterialTheme.colorScheme.tertiaryContainer }, "tertiaryContainer")
    ColorItem({ MaterialTheme.colorScheme.onTertiaryContainer }, "onTertiaryContainer")
    ColorItem({ MaterialTheme.colorScheme.background }, "background")
    ColorItem({ MaterialTheme.colorScheme.onBackground }, "onBackground")
    ColorItem({ MaterialTheme.colorScheme.surface }, "surface")
    ColorItem({ MaterialTheme.colorScheme.onSurface }, "onSurface")
    ColorItem({ MaterialTheme.colorScheme.surfaceVariant }, "surfaceVariant")
    ColorItem({ MaterialTheme.colorScheme.onSurfaceVariant }, "onSurfaceVariant")
    ColorItem({ MaterialTheme.colorScheme.surfaceTint }, "surfaceTint")
    ColorItem({ MaterialTheme.colorScheme.inverseSurface }, "inverseSurface")
    ColorItem({ MaterialTheme.colorScheme.inverseOnSurface }, "inverseOnSurface")
    ColorItem({ MaterialTheme.colorScheme.error }, "error")
    ColorItem({ MaterialTheme.colorScheme.onError }, "onError")
    ColorItem({ MaterialTheme.colorScheme.errorContainer }, "errorContainer")
    ColorItem({ MaterialTheme.colorScheme.onErrorContainer }, "onErrorContainer")
    ColorItem({ MaterialTheme.colorScheme.outline }, "outline")
    ColorItem({ MaterialTheme.colorScheme.outlineVariant }, "outlineVariant")
    ColorItem({ MaterialTheme.colorScheme.scrim }, "scrim")
  }
}

@Composable
private fun ColorItem(color: @Composable () -> Color, colorName: String) {
  Row() {
    Box(Modifier.weight(1f)) {
      HedvigTheme(darkTheme = false) {
        Surface(Modifier.fillMaxSize().height(50.dp), color = color()) {
          Box(contentAlignment = Alignment.Center) {
            Text(colorName)
          }
        }
      }
    }
    Box(Modifier.weight(1f)) {
      HedvigTheme(darkTheme = true) {
        Surface(Modifier.fillMaxSize().height(50.dp), color = color()) {
          Box(contentAlignment = Alignment.Center) {
            Text(colorName)
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewHedvigColorScheme() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigColorScheme()
    }
  }
}
