package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import kotlin.time.Instant

/** [instant] as a screen reader reads it out, with date, time and year. */
@Composable
internal expect fun talkBackDateTime(instant: Instant): String
