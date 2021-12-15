package com.hedvig.app.ui.compose.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
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
        colorResource(R.color.lavender_400) -> Color.Black
        colorResource(R.color.forever_orange_500) -> Color.Black
        colorResource(R.color.colorWarning) -> Color.Black
        Color.Transparent -> contentColorFor(MaterialTheme.colors.background)
        else -> contentColorFor(backgroundColor)
    }
}
