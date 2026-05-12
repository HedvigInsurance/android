package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

// Implemented by Swift. While a pointer is down on a marked region we
// disable the system swipe-back so Compose's horizontal scroll can win;
// on touch end we re-enable it.
interface IosSwipeBackController {
  fun setSwipeBackEnabled(isEnabled: Boolean)
}

private val LocalIosSwipeBackController = staticCompositionLocalOf<IosSwipeBackController?> { null }

@Composable
actual fun Modifier.blockSwipeBackOnIos(): Modifier {
  val controller = LocalIosSwipeBackController.current ?: return this
  return this.pointerInput(controller) {
    awaitPointerEventScope {
      while (true) {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        controller.setSwipeBackEnabled(false)
        try {
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
