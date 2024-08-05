package com.hedvig.android.feature.chat.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.ui.HedvigDateTimeFormatterDefaults
import com.hedvig.android.feature.chat.model.ChatMessage
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
internal fun ChatMessage.backgroundColor(): Color = when (sender) {
  ChatMessage.Sender.HEDVIG -> MaterialTheme.colorScheme.surface
  ChatMessage.Sender.MEMBER -> MaterialTheme.colorScheme.infoContainer
}

@Composable
internal fun ChatMessage.onBackgroundColor(): Color = when (sender) {
  ChatMessage.Sender.HEDVIG -> MaterialTheme.colorScheme.onSurface
  ChatMessage.Sender.MEMBER -> MaterialTheme.colorScheme.onInfoContainer
}

internal fun ChatMessage.messageHorizontalAlignment(): Alignment.Horizontal = when (sender) {
  ChatMessage.Sender.HEDVIG -> Alignment.Start
  ChatMessage.Sender.MEMBER -> Alignment.End
}

internal fun ChatMessage.formattedDateTime(locale: Locale): String {
  return sentAt.formattedChatDateTime(locale)
}

internal fun Instant.formattedChatDateTime(locale: Locale): String {
  val sentAt = this
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
  return nowLocalDateTime.toJavaLocalDateTime().with(TemporalAdjusters.firstDayOfYear())
    .toKotlinLocalDateTime().date.atStartOfDayIn(timeZone)
}
