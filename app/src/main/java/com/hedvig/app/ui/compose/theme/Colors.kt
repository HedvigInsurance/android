package com.hedvig.app.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

val lavender200: Color
    @Composable get() = colorResource(R.color.lavender_200)
val lavender300: Color
    @Composable get() = colorResource(R.color.lavender_300)
val lavender400: Color
    @Composable get() = colorResource(R.color.lavender_400)
val lavender900: Color
    @Composable get() = colorResource(R.color.lavender_900)

val foreverOrange300: Color
    @Composable get() = colorResource(R.color.forever_orange_300)
val foreverOrange500: Color
    @Composable get() = colorResource(R.color.forever_orange_500)

object ClaimStatusColors {
    object Pill {
        val paid: Color
            @Composable get() = if (isSystemInDarkTheme()) {
                lavender400
            } else {
                lavender200
            }

        val reopened: Color
            @Composable get() = if (isSystemInDarkTheme()) {
                foreverOrange500
            } else {
                foreverOrange300
            }
    }

    object Progress {
        val paid: Color
            @Composable get() = colorResource(R.color.colorSecondary)

        val reopened: Color
            @Composable get() = foreverOrange500
    }
}

@Composable
fun hedvigContentColorFor(backgroundColor: Color): Color {
    // TODO check how to properly provide contentColor colors for non-material colors. This is a workaround for now.
    return when (backgroundColor) {
        foreverOrange500 -> Color.Black
        else -> contentColorFor(backgroundColor)
    }
}
