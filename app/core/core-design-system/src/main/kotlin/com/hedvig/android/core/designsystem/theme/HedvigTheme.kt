package com.hedvig.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HedvigTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  HedvigMaterial3Theme(
    darkTheme = darkTheme,
    content = content,
  )
}
