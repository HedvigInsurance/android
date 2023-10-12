package com.hedvig.android.feature.chat.legacy

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import dev.chrisbanes.insetter.applyInsetter

internal fun View.show(): View {
  if (visibility != View.VISIBLE) {
    visibility = View.VISIBLE
  }
  return this
}

internal fun View.hide(): View {
  if (visibility != View.INVISIBLE) {
    visibility = View.INVISIBLE
  }
  return this
}

internal fun View.remove(): View {
  if (visibility != View.GONE) {
    this.visibility = View.GONE
  }
  return this
}

internal fun View.fadeIn(endAction: (() -> Unit)? = null) {
  alpha = 0f
  show()
  val animation = animate().setDuration(225).alpha(1f)
  endAction?.let { animation.withEndAction(it) }
  animation.start()
}

internal fun View.fadeOut(endAction: (() -> Unit)? = null, removeOnEnd: Boolean = true) {
  alpha = 1f
  show()
  val animation = animate().setDuration(225).alpha(0f)
  animation.withEndAction {
    if (removeOnEnd) {
      this.remove()
    }
    endAction?.invoke()
  }
  animation.start()
}

internal fun View.applyStatusBarInsets() = applyInsetter {
  type(statusBars = true) {
    padding()
  }
}

fun View.dismissKeyboard() =
  (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
    windowToken,
    0,
  )
