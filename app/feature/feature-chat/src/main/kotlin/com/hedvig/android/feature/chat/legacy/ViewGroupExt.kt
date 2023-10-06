package com.hedvig.android.feature.chat.legacy

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.ViewGroup

// calculates keyboard height for non fullscreen views
@SuppressLint("InternalInsetResource", "DiscouragedApi") // This is very fragile to be removed with the chat redesign
fun ViewGroup.calculateNonFullscreenHeightDiff(): Int {
  val r = Rect()
  this.getWindowVisibleDisplayFrame(r)

  val screenHeight = this.rootView.height
  var heightDifference = screenHeight - (r.bottom - r.top)
  val resourceId = resources
    .getIdentifier(
      "status_bar_height",
      "dimen",
      "android",
    )
  if (resourceId > 0) {
    heightDifference -= resources
      .getDimensionPixelSize(resourceId)
  }
  return heightDifference
}
