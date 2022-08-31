package com.hedvig.android.core.ui

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.debugBorder(color: Color = Color.Red, dp: Dp = 1.dp): Modifier = border(dp, color)
