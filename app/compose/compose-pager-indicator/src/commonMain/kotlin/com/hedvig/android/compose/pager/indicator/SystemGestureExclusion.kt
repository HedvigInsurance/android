package com.hedvig.android.compose.pager.indicator

import androidx.compose.ui.Modifier

/** Keeps the system back gesture from stealing horizontal drags on this element, on platforms that have one. */
expect fun Modifier.excludeFromSystemGestures(): Modifier
