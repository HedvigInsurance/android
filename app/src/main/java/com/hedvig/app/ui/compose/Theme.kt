package com.hedvig.app.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightThemeColors = Colors(
    primary = hedvigBlack,
    primaryVariant = hedvigDarkGray,
    onPrimary = Color.White,
    secondary = lavender300,
    secondaryVariant = lavender300,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = hedvigBlack,
    background = background,
    onBackground = hedvigBlack,
    error = errorLight,
    onError = Color.White,
    isLight = true
)

private val darkThemeColors = Colors(
    primary = Color.White,
    primaryVariant = hedvigOffWhite,
    onPrimary = hedvigBlack,
    secondary = lavender400,
    secondaryVariant = lavender400,
    onSecondary = Color.White,
    surface = surfaceDark,
    onSurface = Color.White,
    background = hedvigBlack,
    onBackground = hedvigOffWhite,
    error = errorDark,
    onError = Color.White,
    isLight = false
)

@Composable
fun HedvigTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) darkThemeColors else lightThemeColors,
        content = content
    )
}
