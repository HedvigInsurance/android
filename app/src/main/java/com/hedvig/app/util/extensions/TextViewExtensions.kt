package com.hedvig.app.util.extensions

import android.graphics.Paint
import android.widget.TextView
import io.noties.markwon.Markwon

fun TextView.setStrikethrough(strikethrough: Boolean) {
    paintFlags = if (strikethrough) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG
    }
}

fun TextView.setMarkdownText(text: String) {
    Markwon
        .create(context)
        .setMarkdown(this, text)
}
