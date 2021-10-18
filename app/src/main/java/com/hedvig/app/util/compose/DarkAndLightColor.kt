package com.hedvig.app.util.compose

import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.google.android.material.color.MaterialColors
import com.hedvig.app.R

sealed class DarkAndLightColor {

    @Composable
    fun toComposableColor(): Color {
        return when (this) {
            is FromCompose -> this.getColor(isSystemInDarkTheme())
            is FromResources -> colorResource(this.getColorRes(isSystemInDarkTheme()))
            is FromTheme -> {
                val colorRes = MaterialColors.getColor(
                    LocalContext.current,
                    this.getColorThemeAttr(isSystemInDarkTheme()),
                    ""
                )
                Color(colorRes)
            }
        }
    }

    private class FromCompose(val dark: Color, val light: Color) : DarkAndLightColor() {
        fun getColor(isInDarkTheme: Boolean): Color = if (isInDarkTheme) dark else light
    }

    private class FromResources(@ColorRes val dark: Int, @ColorRes val light: Int) : DarkAndLightColor() {
        @ColorRes
        fun getColorRes(isInDarkTheme: Boolean): Int = if (isInDarkTheme) dark else light
    }

    private class FromTheme(@AttrRes val dark: Int, @AttrRes val light: Int) : DarkAndLightColor() {
        @AttrRes
        fun getColorThemeAttr(isInDarkTheme: Boolean): Int = if (isInDarkTheme) dark else light
    }

    companion object {
        operator fun invoke(
            @ColorRes dark: Int,
            @ColorRes light: Int
        ): DarkAndLightColor = FromResources(dark, light)

        @JvmName("invokeFromColorRes")
        operator fun invoke(
            @ColorRes both: Int,
        ): DarkAndLightColor = invoke(both, both)

        operator fun invoke(
            dark: Color,
            light: Color,
        ): DarkAndLightColor = FromCompose(dark, light)

        operator fun invoke(
            both: Color
        ): DarkAndLightColor = invoke(both, both)

        fun primary(): DarkAndLightColor = FromTheme(R.attr.colorPrimary, R.attr.colorPrimary)
    }
}
