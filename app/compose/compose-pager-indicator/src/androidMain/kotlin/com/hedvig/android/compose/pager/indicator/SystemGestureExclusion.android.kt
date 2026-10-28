package com.hedvig.android.compose.pager.indicator

import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.ui.Modifier

internal actual fun Modifier.excludeFromSystemGestures(): Modifier = systemGestureExclusion()
