package com.hedvig.app.util.extensions

import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import com.hedvig.app.util.extensions.view.updateMargin
import dev.chrisbanes.insetter.Insetter

fun View.insetSystemBottomWithMargin() {
    Insetter.builder().setOnApplyInsetsListener { view, insets, _ ->
        view.updateMargin(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
    }.applyToView(this)
}

fun View.insetSystemTopWithMargin() {
    Insetter.builder().setOnApplyInsetsListener { view, insets, _ ->
        view.updateMargin(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
    }.applyToView(this)
}

fun View.insetSystemTopWithPadding() {
    Insetter.builder().setOnApplyInsetsListener { view, insets, initialState ->
        view.updatePaddingRelative(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
    }.applyToView(this)
}
