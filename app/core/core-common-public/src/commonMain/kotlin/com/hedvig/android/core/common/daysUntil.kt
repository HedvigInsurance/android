package com.hedvig.android.core.common

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

fun daysUntil(localDate: LocalDate): Int {
  val timeZone = TimeZone.currentSystemDefault()
  val startOfToday = Clock.System.now().toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)
  val startOfDayOfRenewal = localDate.atStartOfDayIn(timeZone)
  return startOfToday.daysUntil(startOfDayOfRenewal, timeZone)
}
