package com.hedvig.app.feature.loggedin.ui

import android.graphics.drawable.GradientDrawable

class GradientDrawableWrapper : GradientDrawable() {
    private var cachedColors: IntArray? = null

    fun getColorsLowerApi() = cachedColors

    override fun setColors(colors: IntArray?) {
        cachedColors = colors
        super.setColors(colors)
    }
}
