package com.hedvig.app.util.extensions

import android.graphics.Paint
import android.widget.TextView
import androidx.annotation.DrawableRes
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

fun TextView.setCompoundDrawable(
    @DrawableRes start: Int? = null,
    @DrawableRes top: Int? = null,
    @DrawableRes bottom: Int? = null,
    @DrawableRes end: Int? = null,
) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        start?.let { context.compatDrawable(it) },
        top?.let { context.compatDrawable(it) },
        end?.let { context.compatDrawable(it) },
        bottom?.let { context.compatDrawable(it) },
    )
}
