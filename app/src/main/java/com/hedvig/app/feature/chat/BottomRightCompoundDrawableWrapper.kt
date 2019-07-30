package com.hedvig.app.feature.chat

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class BottomRightCompoundDrawableWrapper(
    private val drawable: Drawable,
    private val paddingEnd: Int,
    private val paddingBottom: Int) : Drawable() {


    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight

    // CanvasSize is correct here
    @SuppressLint("CanvasSize")
    override fun draw(canvas: Canvas) {
        val halfCanvas = canvas.height.toFloat() / 2
        val halfDrawableHeight = drawable.intrinsicHeight / 2
        val halfDrawableWidth = drawable.intrinsicWidth / 2

        // align to bottom right
        canvas.save()
        canvas.translate(halfDrawableWidth.toFloat() - paddingEnd, halfCanvas - paddingBottom - halfDrawableHeight)
        drawable.draw(canvas)
        canvas.restore()
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // not supported
    }
}
