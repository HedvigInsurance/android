package com.hedvig.app.util.extensions

import android.graphics.Paint
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
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

fun TextView.putCompoundDrawablesRelativeWithIntrinsicBounds(
    @DrawableRes start: Int = 0,
    @DrawableRes top: Int = 0,
    @DrawableRes end: Int = 0,
    @DrawableRes bottom: Int = 0,
) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        start,
        top,
        end,
        bottom
    )
}

fun TextView.onImeAction(
    imeActionId: Int = EditorInfo.IME_ACTION_DONE,
    triggerOnEnter: Boolean = true,
    action: () -> Unit,
) {
    setOnEditorActionListener(
        TextView.OnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == imeActionId) {
                action()
                return@OnEditorActionListener true
            }
            if (triggerOnEnter && actionId == EditorInfo.IME_NULL && keyEvent.action == KeyEvent.ACTION_DOWN) {
                action()
                return@OnEditorActionListener true
            }
            false
        }
    )
}
