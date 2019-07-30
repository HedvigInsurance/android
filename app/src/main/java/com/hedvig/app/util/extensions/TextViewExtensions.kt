package com.hedvig.app.util.extensions

import android.graphics.Paint
import android.widget.TextView

fun TextView.setStrikethrough(strikethrough: Boolean) {
    if (strikethrough) {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG
    }
}
