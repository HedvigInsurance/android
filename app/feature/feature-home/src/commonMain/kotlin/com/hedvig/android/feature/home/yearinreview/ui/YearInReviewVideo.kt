package com.hedvig.android.feature.home.yearinreview.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Plays the video at [url] muted and on a loop, cropped to fill its bounds, and holds it while [isPaused]. */
@Composable
internal expect fun YearInReviewVideo(url: String, isPaused: Boolean, modifier: Modifier = Modifier)
