package com.hedvig.android.data.display.items

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

data class DisplayItem(
  val title: String,
  val value: DisplayItemValue,
) {
  sealed interface DisplayItemValue {
    data class DateTime(val localDateTime: LocalDateTime) : DisplayItemValue

    data class Date(val date: LocalDate) : DisplayItemValue

    data class Text(val text: String) : DisplayItemValue
  }

  companion object {
    fun fromStrings(title: String, value: String): DisplayItem {
      val displayItemValue: DisplayItemValue = run {
        try {
          val localDate = kotlinx.datetime.LocalDate.parse(value)
          return@run DisplayItemValue.Date(localDate)
        } catch (_: IllegalArgumentException) {
        }
        try {
          val localDateTime = kotlinx.datetime.LocalDateTime.parse(value)
          return@run DisplayItemValue.DateTime(localDateTime)
        } catch (_: IllegalArgumentException) {
        }
        try {
          val instant = Instant.parse(value)
          return@run DisplayItemValue.DateTime(
            instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
          )
        } catch (_: IllegalArgumentException) {
        }
        DisplayItemValue.Text(value)
      }
      return DisplayItem(
        title,
        displayItemValue,
      )
    }
  }
}
