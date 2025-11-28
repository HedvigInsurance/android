package com.hedvig.android.design.system.hedvig

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface DateFormatter {
  fun format(date: LocalDate): String
  fun format(date: LocalDateTime): String
}
