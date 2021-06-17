package com.hedvig.app.util.extensions

import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.hedvig.app.R
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.linkify.LinkifyPlugin

fun TextView.setStrikethrough(strikethrough: Boolean) {
    paintFlags = if (strikethrough) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG
    }
}

fun TextView.setMarkdownText(text: String) {
    Markwon
        .builder(context)
        .usePlugins(
            listOf(
                CorePlugin.create(),
                LinkifyPlugin.create(),
                object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { view, link ->
                            runCatching {
                                val uri = Uri.parse(link)
                                if (view.context.canOpenUri(uri)) {
                                    view.context.openUri(uri)
                                }
                            }
                        }
                    }
                }
            )
        )
        .build()
        .setMarkdown(this, text)
}

fun TextView.createWebLink(link: String) {
    movementMethod = LinkMovementMethod.getInstance()
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(link, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(link)
    }
    setLinkTextColor(context.getColor(R.color.textColorLink))
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
    triggerOnEnter: Boolean = imeActionId == EditorInfo.IME_ACTION_DONE,
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
