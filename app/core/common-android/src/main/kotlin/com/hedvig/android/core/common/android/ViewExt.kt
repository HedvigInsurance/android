package com.hedvig.android.core.common.android

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView

fun View.disable() {
  isEnabled = false
  alpha = 0.2f
}

fun View.enable() {
  isEnabled = true
  alpha = 1f
}

fun View.show(): View {
  if (visibility != View.VISIBLE) {
    visibility = View.VISIBLE
  }
  return this
}

fun View.hide(): View {
  if (visibility != View.INVISIBLE) {
    visibility = View.INVISIBLE
  }
  return this
}

fun View.remove(): View {
  if (visibility != View.GONE) {
    this.visibility = View.GONE
  }
  return this
}

fun NestedScrollView.setupToolbarScrollListener(toolbar: Toolbar) {
  setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
    val maxElevationScroll = 200
    val offset = this.computeVerticalScrollOffset().toFloat()
    val percentage = if (offset < maxElevationScroll) {
      offset / maxElevationScroll
    } else {
      1f
    }
    toolbar.elevation = percentage * 10
  }
}
