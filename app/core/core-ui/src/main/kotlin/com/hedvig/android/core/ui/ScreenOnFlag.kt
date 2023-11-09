package com.hedvig.android.core.ui

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import hedvig.resources.R

/**
 * While this composable is in composition the phone screen stays awake. This is automatically cleared when the
 * composable leaves the composition.
 * Keeps an internal ref count scoped to the current [View] to make sure that multiple calls to this composable don't
 * negate other callers.
 */
@Composable
fun ScreenOnFlag() {
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
