package com.hedvig.android.feature.chat.cbm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.ui.HedvigDateTimeFormatterDefaults
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.Sender
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CbmChatMessage.backgroundColor(): Color = when (sender) {
  Sender.HEDVIG -> MaterialTheme.colorScheme.surface
  Sender.MEMBER -> MaterialTheme.colorScheme.infoContainer
}

@Composable
internal fun CbmChatMessage.onBackgroundColor(): Color = when (sender) {
  Sender.HEDVIG -> MaterialTheme.colorScheme.onSurface
  Sender.MEMBER -> MaterialTheme.colorScheme.onInfoContainer
}

// Align by the sender if present, otherwise alternate between the two
internal fun CbmChatMessage?.messageHorizontalAlignment(index: Int): Alignment.Horizontal = when (this?.sender) {
  null -> if (index % 2 == 0) Alignment.Start else Alignment.End
  Sender.HEDVIG -> Alignment.Start
  Sender.MEMBER -> Alignment.End
}

internal fun CbmChatMessage.formattedDateTime(locale: Locale): String {
  val now = Clock.System.now()
  val timeZone = TimeZone.currentSystemDefault()
  val nowLocalDateTime: LocalDateTime = now.toLocalDateTime(timeZone)
  val todayAtStartOfDay = nowLocalDateTime.date.atStartOfDayIn(timeZone)
  val formatter = when {
    sentAt > todayAtStartOfDay -> HedvigDateTimeFormatterDefaults.timeOnly(locale)
    sentAt > todayAtStartOfDay.minus(3.days) -> HedvigDateTimeFormatterDefaults.dayOfTheWeekAndTime(locale)
    sentAt > getStartOfThisYear(nowLocalDateTime, timeZone) -> HedvigDateTimeFormatterDefaults.monthDateAndTime(locale)
    else -> HedvigDateTimeFormatterDefaults.yearMonthDateAndTime(locale)
  }
  return formatter.format(sentAt.toLocalDateTime(timeZone).toJavaLocalDateTime())
}

private fun getStartOfThisYear(nowLocalDateTime: LocalDateTime, timeZone: TimeZone): Instant {
  return nowLocalDateTime
    .toJavaLocalDateTime()
    .with(TemporalAdjusters.firstDayOfYear())
    .toKotlinLocalDateTime()
    .date
    .atStartOfDayIn(timeZone)
}
