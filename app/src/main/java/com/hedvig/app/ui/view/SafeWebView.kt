package com.hedvig.app.ui.view

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

class SafeWebView : WebView {
    constructor(context: Context) : super(safeContext(context))
    constructor(context: Context, attributeSet: AttributeSet?) : super(
        safeContext(context),
        attributeSet
    )

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        safeContext(
            context
        ),
        attributeSet, defStyle
    )

    companion object {
        private fun safeContext(context: Context) =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                context.createConfigurationContext(Configuration())
            } else {
                context
            }
    }
}
