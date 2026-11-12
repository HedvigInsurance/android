package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hedvig.android.design.system.hedvig.datepicker.formatInstantForTalkBack
import kotlin.time.Instant

@Composable
internal actual fun talkBackDateTime(instant: Instant): String = formatInstantForTalkBack(LocalContext.current, instant)
