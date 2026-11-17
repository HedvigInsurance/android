package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.util.Locale
import java.util.TimeZone as JavaTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.After
import org.junit.Before
import org.junit.Test

class TravelDatePickerStateTest {
  private val defaultZone = JavaTimeZone.getDefault()

  @Before
  fun setUp() {
    // A zone behind UTC, where reading picker millis in the device zone gives the previous day.
    JavaTimeZone.setDefault(JavaTimeZone.getTimeZone("America/Los_Angeles"))
  }

  @After
  fun tearDown() {
    JavaTimeZone.setDefault(defaultZone)
  }

  private fun utcMillis(date: LocalDate): Long = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

  @Test
  fun `the picker allows exactly the given range`() {
    val picker = travelDatePickerState(
      locale = Locale.forLanguageTag("sv-SE"),
      minDate = LocalDate(2026, 11, 10),
      maxDate = LocalDate(2027, 1, 16),
      selectedDate = LocalDate(2026, 11, 17),
    )
    assertThat(picker.yearRange).isEqualTo(2026..2027)
    val selectable = picker.selectableDates
    assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2026, 11, 9)))).isFalse()
    assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2026, 11, 10)))).isTrue()
    assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2027, 1, 16)))).isTrue()
    assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2027, 1, 17)))).isFalse()
    assertThat(selectable.isSelectableYear(2025)).isFalse()
    assertThat(selectable.isSelectableYear(2026)).isTrue()
    assertThat(selectable.isSelectableYear(2027)).isTrue()
    assertThat(selectable.isSelectableYear(2028)).isFalse()
  }

  @Test
  fun `the selected date goes in and comes out as the same calendar day`() {
    val picker = travelDatePickerState(
      locale = Locale.forLanguageTag("sv-SE"),
      minDate = LocalDate(2026, 11, 10),
      maxDate = LocalDate(2027, 1, 16),
      selectedDate = LocalDate(2026, 12, 24),
    )
    assertThat(picker.selectedDateMillis).isEqualTo(utcMillis(LocalDate(2026, 12, 24)))
    assertThat(utcMillisToDate(picker.selectedDateMillis!!)).isEqualTo(LocalDate(2026, 12, 24))
  }

  @Test
  fun `the picker opens on the month of the selected date`() {
    val picker = travelDatePickerState(
      locale = Locale.forLanguageTag("sv-SE"),
      minDate = LocalDate(2040, 1, 1),
      maxDate = LocalDate(2040, 12, 31),
      selectedDate = LocalDate(2040, 3, 14),
    )
    assertThat(picker.displayedMonthMillis).isEqualTo(utcMillis(LocalDate(2040, 3, 1)))
  }
}
