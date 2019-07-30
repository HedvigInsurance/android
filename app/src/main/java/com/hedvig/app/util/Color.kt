package com.hedvig.app.util

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils
import com.hedvig.android.owldroid.type.HedvigColor
import com.hedvig.app.R
import kotlin.math.abs

@ColorInt
fun percentageFade(@ColorInt from: Int, @ColorInt to: Int, percentage: Float): Int {
    val fromAlpha = Color.alpha(from)
    val fromRed = Color.red(from)
    val fromGreen = Color.green(from)
    val fromBlue = Color.blue(from)

    val toAlpha = Color.alpha(to)
    val toRed = Color.red(to)
    val toGreen = Color.green(to)
    val toBlue = Color.blue(to)

    val diffAlpha = Math.abs(toAlpha - fromAlpha)
    val diffRed = Math.abs(toRed - fromRed)
    val diffGreen = Math.abs(toGreen - fromGreen)
    val diffBlue = Math.abs(toBlue - fromBlue)

    val factorAlpha = (diffAlpha * percentage).toInt()
    val factorRed = (diffRed * percentage).toInt()
    val factorGreen = (diffGreen * percentage).toInt()
    val factorBlue = (diffBlue * percentage).toInt()

    var resAlpha = if (fromAlpha > toAlpha) fromAlpha - factorAlpha else fromAlpha + factorAlpha
    var resRed = if (fromRed > toRed) fromRed - factorRed else fromRed + factorRed
    var resGreen = if (fromGreen > toGreen) fromGreen - factorGreen else fromGreen + factorGreen
    var resBlue = if (fromBlue > toBlue) fromBlue - factorBlue else fromBlue + factorBlue

    resAlpha = when (toAlpha) {
        in resAlpha until fromAlpha -> toAlpha
        in (fromAlpha + 1)..resAlpha -> toAlpha
        else -> resAlpha
    }

    resRed = when (toRed) {
        in resRed until fromRed -> toRed
        in (fromRed + 1)..resRed -> toRed
        else -> resRed
    }

    resGreen = when (toGreen) {
        in resGreen until fromGreen -> toGreen
        in (fromGreen + 1)..resGreen -> toGreen
        else -> resGreen
    }

    resBlue = when (toBlue) {
        in resBlue until fromBlue -> toBlue
        in (fromBlue + 1)..resBlue -> toBlue
        else -> resBlue
    }

    return Color.argb(resAlpha, resRed, resGreen, resBlue)
}

@ColorRes
fun HedvigColor.mappedColor(): Int = when (this) {
    HedvigColor.DARKPURPLE -> R.color.dark_purple
    HedvigColor.LIGHTGRAY -> R.color.light_gray
    HedvigColor.OFFWHITE -> R.color.off_white
    HedvigColor.DARKGRAY -> R.color.gray
    HedvigColor.PURPLE -> R.color.purple
    HedvigColor.WHITE -> R.color.white
    HedvigColor.OFFBLACK -> R.color.off_black
    HedvigColor.BLACK -> R.color.black
    HedvigColor.TURQUOISE -> R.color.green
    HedvigColor.PINK -> R.color.pink
    HedvigColor.BLACKPURPLE -> R.color.off_black_dark
    HedvigColor.YELLOW -> R.color.yellow
    HedvigColor.`$UNKNOWN` -> R.color.purple
}

val colorArray = arrayOf(
    R.color.purple,
    R.color.dark_purple,
    R.color.green,
    R.color.dark_green,
    R.color.pink,
    R.color.maroon,
    R.color.yellow
)

@ColorRes
fun hashColor(obj: Any) = colorArray[abs(obj.hashCode()) % colorArray.size]

@ColorInt
fun lightenColor(@ColorInt color: Int, factor: Float): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color, hsl)

    hsl[2] += factor
    hsl[2] = Math.max(0f, Math.min(hsl[2], 1f))

    return ColorUtils.HSLToColor(hsl)
}

enum class LightClass {
    DARK,
    LIGHT
}

fun getLightness(@ColorInt color: Int) = if (ColorUtils.calculateLuminance(color) < 0.5) {
    LightClass.DARK
} else {
    LightClass.LIGHT
}
