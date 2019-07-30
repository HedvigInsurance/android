package com.hedvig.app.util

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {
    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }
}
