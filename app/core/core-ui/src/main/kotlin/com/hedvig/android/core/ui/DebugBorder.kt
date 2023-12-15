package com.hedvig.android.core.ui

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("unused") // Used when quickly prototyping something and want to see how it renders
fun Modifier.debugBorder(color: Color = Color.Red, dp: Dp = 1.dp): Modifier = this.border(dp, color)
