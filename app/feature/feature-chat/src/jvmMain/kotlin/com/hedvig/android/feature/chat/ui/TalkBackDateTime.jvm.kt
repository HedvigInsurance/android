package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal actual fun talkBackDateTime(instant: Instant): String {
  return HedvigDateTimeFormatterDefaults
    .yearMonthDateAndTime(getLocale())
    .format(instant.toLocalDateTime(TimeZone.currentSystemDefault()))
}
