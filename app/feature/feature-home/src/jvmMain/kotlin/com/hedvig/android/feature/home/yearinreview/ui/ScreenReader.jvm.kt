package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.runtime.Composable

/** The desktop app has no way to tell whether a screen reader is running. */
@Composable
internal actual fun rememberIsScreenReaderOn(): Boolean = false
