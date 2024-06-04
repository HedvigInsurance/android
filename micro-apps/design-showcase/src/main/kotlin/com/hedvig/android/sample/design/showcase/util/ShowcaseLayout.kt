package com.hedvig.android.sample.design.showcase.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

/**
 * A layout that takes care of rendering the content both in light and dark mode, and allows you to use a 2d scroll to
 * see everything.
 */
@Composable
internal fun ShowcaseLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Row(
    modifier
      .background(HedvigTheme.colorScheme.surfacePrimary)
      .fillMaxSize()
      .freeScroll(rememberFreeScrollState())
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(16.dp),
  ) {
    HedvigTheme(false) {
      Content(content)
    }
    HedvigTheme(true) {
      Content(content)
    }
  }
}

@Composable
private fun Content(content: @Composable () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    shape = HedvigTheme.shapes.cornerXXLarge,
  ) {
    Box(
      Modifier
        .padding(8.dp)
        .dashedBorder(Color(0xFF9747FF), HedvigTheme.shapes.cornerXLarge)
        .padding(8.dp),
    ) {
      content()
    }
  }
}
