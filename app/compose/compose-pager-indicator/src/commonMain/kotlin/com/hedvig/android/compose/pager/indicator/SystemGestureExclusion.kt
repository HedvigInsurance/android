package com.hedvig.android.compose.pager.indicator

import androidx.compose.ui.Modifier

/** Keeps system gestures from stealing horizontal drags on this element, on platforms that have them. */
internal expect fun Modifier.excludeFromSystemGestures(): Modifier
