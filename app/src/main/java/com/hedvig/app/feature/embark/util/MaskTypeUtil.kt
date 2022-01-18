package com.hedvig.app.feature.embark.util

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

object MaskTypeUtil {

    fun getYearsBetween(text: String, currentDate: LocalDate, pattern: String): String {
        val dateTimeFormatter = createDateFormatterFromPattern(pattern)
        val birthDate = LocalDate.parse(text, dateTimeFormatter)
        return Period.between(birthDate, currentDate).years.toString()
    }

    private fun createDateFormatterFromPattern(pattern: String): DateTimeFormatter {
        return DateTimeFormatterBuilder()
            .appendValueReduced(
                ChronoField.YEAR_OF_ERA, 2, 4, LocalDate.of(1925, 1, 1)
            )
            .appendPattern(pattern)
            .toFormatter()
    }
}
