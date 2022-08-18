package com.hedvig.android.core.designsystem.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

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
val progressBlue = Color(0xffC3CBD6)
val progressYellow = Color(0xffEDCDAB)

@Suppress("unused")
val Colors.onWarning: Color
  get() = hedvigBlack

val Colors.separator: Color
  get() = if (isLight) {
    hedvigBlack.copy(alpha = 0.12f)
  } else {
    hedvigOffWhite.copy(alpha = 0.12f)
  }
