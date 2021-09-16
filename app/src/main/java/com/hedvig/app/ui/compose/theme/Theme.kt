package com.hedvig.app.ui.compose.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.android.material.composethemeadapter.createMdcTheme

@Composable
fun HedvigTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (colors, type, shapes) = createMdcTheme(
        context = context,
        layoutDirection = layoutDirection,
        setTextColors = true,
        setDefaultFontFamily = true
    )

    MaterialTheme(
        colors = colors!!,
        typography = type!!,
        shapes = shapes!!,
        content = content,
    )
}
