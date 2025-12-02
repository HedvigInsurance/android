package com.hedvig.feature.claim.chat.ui.audiorecording

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import hedvig.resources.R

@Composable
internal actual fun ScreenOnFlag() {
  val view = LocalView.current
  DisposableEffect(view) {
    val keepScreenOnState = view.keepScreenOnState
    keepScreenOnState.request()
    onDispose {
      keepScreenOnState.release()
    }
  }
}

private val View.keepScreenOnState: KeepScreenOnState
  get() = getTag(R.id.keep_screen_on_state) as? KeepScreenOnState
    ?: KeepScreenOnState(this).also { setTag(R.id.keep_screen_on_state, it) }

private class KeepScreenOnState(private val view: View) {
  private var refCount = 0
    set(value) {
      val newValue = value.coerceAtLeast(0)
      field = newValue
      view.keepScreenOn = newValue > 0
    }

  fun request() {
    refCount++
  }

  fun release() {
    refCount--
  }
}
