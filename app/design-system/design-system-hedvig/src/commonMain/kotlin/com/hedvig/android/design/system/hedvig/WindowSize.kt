package com.hedvig.android.design.system.hedvig

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun WindowSizeClass.Companion.calculateForPreview(): WindowSizeClass {
  val screenSize = getScreenSizeDp()
  return calculateFromSize(screenSize)
}

@Composable
fun getScreenSizeDp(): DpSize {
  val configuration = LocalWindowInfo.current.containerSize
  return DpSize(configuration.width.dp, configuration.height.dp)
}
