package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.runtime.Composable

/** Whether a screen reader such as TalkBack is on, updating when it is turned on or off. */
@Composable
internal expect fun rememberIsScreenReaderOn(): Boolean
