package com.hedvig.android.core.ui.preview

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun WindowSizeClass.Companion.calculateForPreview(): WindowSizeClass {
  val configuration = LocalConfiguration.current
  return calculateFromSize(DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp))
}
