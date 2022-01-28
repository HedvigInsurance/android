package com.hedvig.app.ui.compose.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.hedvig.app.R

val hedvigBlack = Color(0xff121212)
val hedvigBlack12percent = hedvigBlack.copy(alpha = 0.12f)
val hedvigOffWhite = Color(0xfffafafa)
val hedvigDarkGray = Color(0xff505050)
val background = Color(0xffF6F6F6)

val whiteHighEmphasis = Color(0xFFFAFAFA)

val errorLight = Color(0xffDD2727)
val errorDark = Color(0xffE24646)

val textColorPrimary = Color(0xAB121212)
val textColorPrimaryDark = Color(0x8FFAFAFA)

val surfaceDark = Color(0xffBE9BF3)

@Composable
fun hedvigContentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        colorResource(R.color.lavender_200) -> colorResource(R.color.hedvig_black)
        colorResource(R.color.lavender_400) -> colorResource(R.color.hedvig_black)
        colorResource(R.color.forever_orange_500) -> colorResource(R.color.hedvig_black)
        colorResource(R.color.colorWarning) -> colorResource(R.color.hedvig_black)
        Color.Transparent -> contentColorFor(MaterialTheme.colors.background)
        else -> contentColorFor(backgroundColor)
    }
}

@Suppress("unused")
val Colors.warning: Color
    @Composable
    @ReadOnlyComposable
    get() = colorResource(R.color.colorWarning)

@Suppress("unused")
val Colors.onWarning: Color
    @Composable
    @ReadOnlyComposable
    get() = colorResource(R.color.hedvig_black)
