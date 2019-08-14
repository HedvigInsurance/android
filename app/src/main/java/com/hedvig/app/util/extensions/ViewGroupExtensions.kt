package com.hedvig.app.util.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

fun ViewGroup.addViews(vararg views: View) = views.forEach { addView(it) }

fun ViewGroup.addViews(views: List<View>) = views.forEach { addView(it) }

val ViewGroup.firstChild
    get() = getChildAt(0)

// calculates keyboard height for non fullscreen views
fun ViewGroup.calculateNonFullscreenHeightDiff(): Int {
    val r = Rect()
    this.getWindowVisibleDisplayFrame(r)

    val screenHeight = this.rootView.height
    var heightDifference = screenHeight - (r.bottom - r.top)
    val resourceId = resources
        .getIdentifier("status_bar_height",
            "dimen", "android")
    if (resourceId > 0) {
        heightDifference -= resources
            .getDimensionPixelSize(resourceId)
    }
    return heightDifference
}
