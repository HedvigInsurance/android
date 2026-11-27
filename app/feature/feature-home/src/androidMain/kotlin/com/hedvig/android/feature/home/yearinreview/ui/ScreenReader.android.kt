package com.hedvig.android.feature.home.yearinreview.ui

import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberIsScreenReaderOn(): Boolean {
  val context = LocalContext.current
  val accessibilityManager = remember(context) { context.getSystemService(AccessibilityManager::class.java) }
  var isOn by remember(accessibilityManager) {
    mutableStateOf(accessibilityManager?.isTouchExplorationEnabled == true)
  }
  DisposableEffect(accessibilityManager) {
    val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled -> isOn = enabled }
    accessibilityManager?.addTouchExplorationStateChangeListener(listener)
    onDispose { accessibilityManager?.removeTouchExplorationStateChangeListener(listener) }
  }
  return isOn
}
