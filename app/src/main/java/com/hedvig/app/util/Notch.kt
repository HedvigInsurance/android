package com.hedvig.app.util

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.view.WindowInsets
import kotlin.math.roundToInt

@Suppress("MagicNumber", "USELESS_CAST")
fun Activity.hasNotch(): Boolean {
    whenApiVersion(Build.VERSION_CODES.P) {
        val windowInsets = window.decorView.rootWindowInsets as WindowInsets? // This one is nullable despite what kotlin thinks
        if (windowInsets != null) {
            val displayCutout = windowInsets.displayCutout
            if (displayCutout != null) {
                return true
            }
        }
    }

    val statusBarHeight = resources.getDimensionPixelSize(
        resources.getIdentifier("status_bar_height", "dimen", "android")
    )
    return (statusBarHeight > convertDpToPixel(24f))
}

fun convertDpToPixel(dp: Float): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return px.roundToInt()
}
