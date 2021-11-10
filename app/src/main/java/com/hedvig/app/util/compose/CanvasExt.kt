package com.hedvig.app.util.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.fillWithColor(color: Color) {
    drawRect(color)
}
