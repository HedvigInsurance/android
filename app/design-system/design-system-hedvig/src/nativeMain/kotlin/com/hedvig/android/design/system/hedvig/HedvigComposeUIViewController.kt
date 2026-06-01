package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.ComposeUIViewController
import com.hedvig.android.design.system.hedvig.api.IosSwipeBackController
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import platform.UIKit.UIViewController

private val LocalIosSwipeBackController = staticCompositionLocalOf<IosSwipeBackController?> { null }

@Suppress("FunctionName")
fun HedvigComposeUIViewController(
  swipeBackController: IosSwipeBackController,
  content: @Composable () -> Unit,
): UIViewController = ComposeUIViewController {
  HedvigTheme {
    CompositionLocalProvider(
      LocalIosSwipeBackController provides swipeBackController,
      LocalMetroViewModelFactory provides IosDiHolder.metroViewModelFactory,
    ) {
      content()
    }
  }
}

@Composable
actual fun Modifier.blockSwipeBackOnIos(): Modifier {
  val controller = LocalIosSwipeBackController.current ?: return this
  return this.pointerInput(controller) {
    awaitPointerEventScope {
      while (true) {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        try {
          controller.setSwipeBackEnabled(false)
          do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.changes.none { it.pressed }) break
          } while (true)
        } finally {
          controller.setSwipeBackEnabled(true)
        }
      }
    }
  }
}
