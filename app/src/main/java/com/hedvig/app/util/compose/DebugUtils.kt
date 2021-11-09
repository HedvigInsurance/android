package com.hedvig.app.util.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.debugBorder(
    color: Color = Color.Red,
    width: Dp = 1.dp,
) = border(border = BorderStroke(width = width, color = color))
