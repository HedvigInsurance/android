package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.chat.model.CbmChatMessage
import com.hedvig.android.feature.chat.model.Sender
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CbmChatMessage.backgroundColor(): Color = when (sender) {
  Sender.HEDVIG -> HedvigTheme.colorScheme.surfacePrimary
  Sender.AUTOMATION -> HedvigTheme.colorScheme.surfacePrimary
  Sender.MEMBER -> HedvigTheme.colorScheme.signalBlueFill
}

@Composable
internal fun CbmChatMessage.onBackgroundColor(): Color = when (sender) {
  Sender.HEDVIG -> HedvigTheme.colorScheme.textPrimary
  Sender.AUTOMATION -> HedvigTheme.colorScheme.textPrimary
  Sender.MEMBER -> HedvigTheme.colorScheme.textBlack
}

// Align by the sender if present, otherwise alternate between the two
internal fun CbmChatMessage?.messageHorizontalAlignment(index: Int): Alignment.Horizontal = when (this?.sender) {
  null -> if (index % 2 == 0) Alignment.Start else Alignment.End
  Sender.HEDVIG -> Alignment.Start
  Sender.AUTOMATION -> Alignment.Start
  Sender.MEMBER -> Alignment.End
}

internal fun CbmChatMessage.formattedDateTime(locale: CommonLocale, now: Instant): String {
  val timeZone = TimeZone.currentSystemDefault()
  val nowLocalDateTime: LocalDateTime = now.toLocalDateTime(timeZone)
  val todayAtStartOfDay = nowLocalDateTime.date.atStartOfDayIn(timeZone)
  val formatter = when {
    sentAt > todayAtStartOfDay -> HedvigDateTimeFormatterDefaults.timeOnly(locale)

    sentAt > todayAtStartOfDay.minus(3.days) -> HedvigDateTimeFormatterDefaults.dayOfTheWeekAndTime(
      locale,
    )

    sentAt > getStartOfThisYear(
      nowLocalDateTime,
      timeZone,
    ) -> HedvigDateTimeFormatterDefaults.monthDateAndTime(locale)

    else -> HedvigDateTimeFormatterDefaults.yearMonthDateAndTime(locale)
  }
  return formatter.format(sentAt.toLocalDateTime(timeZone))
}

private fun getStartOfThisYear(nowLocalDateTime: LocalDateTime, timeZone: TimeZone): Instant {
  return LocalDate(nowLocalDateTime.year, 1, 1).atStartOfDayIn(timeZone)
}

internal fun Instant.formattedChatDateTime(locale: CommonLocale, todayLabel: String, now: Instant): String {
  val sentAt = this
  val timeZone = TimeZone.currentSystemDefault()
  val nowLocalDateTime: LocalDateTime = now.toLocalDateTime(timeZone)
  val todayAtStartOfDay = nowLocalDateTime.date.atStartOfDayIn(timeZone)
  val isToday = sentAt > todayAtStartOfDay
  val formatter = when {
    isToday -> HedvigDateTimeFormatterDefaults.timeOnly(locale)
    sentAt > todayAtStartOfDay.minus(3.days) -> HedvigDateTimeFormatterDefaults.dayOfTheWeekAndTime(locale)
    sentAt > getStartOfThisYear(nowLocalDateTime, timeZone) -> HedvigDateTimeFormatterDefaults.monthDateAndTime(locale)
    else -> HedvigDateTimeFormatterDefaults.yearMonthDateAndTime(locale)
  }
  val formatted = formatter.format(sentAt.toLocalDateTime(timeZone))
  return if (isToday) "$todayLabel $formatted" else formatted
}
