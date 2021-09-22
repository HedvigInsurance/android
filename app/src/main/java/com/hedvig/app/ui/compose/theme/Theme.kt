package com.hedvig.app.ui.compose.theme

import androidx.compose.runtime.Composable
import com.google.android.material.composethemeadapter.MdcTheme

@Composable
fun HedvigTheme(content: @Composable () -> Unit) {
    MdcTheme(setDefaultFontFamily = true, content = content)
}
