package com.hedvig.app.util.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.compatSetTint(@ColorInt color: Int) {
    val drawableWrap = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(drawableWrap, color)
}
