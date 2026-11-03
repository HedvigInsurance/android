package com.hedvig.android.feature.home.yearinreview.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.yearsUntil

/** Whole years since [start], and the days left over after them, as of [today]. */
internal data class MembershipDuration(val years: Int, val days: Int)

internal fun membershipDuration(start: LocalDate, today: LocalDate): MembershipDuration {
  val years = start.yearsUntil(today)
  val clampedAnniversary = start.plus(years, DateTimeUnit.YEAR)
  // A start on 29 February has no anniversary in other years. Adding years lands on 28 February, but
  // [yearsUntil] completes the year only on 1 March, so count the leftover days from there.
  val anniversary = if (clampedAnniversary.day != start.day) {
    clampedAnniversary.plus(1, DateTimeUnit.DAY)
  } else {
    clampedAnniversary
  }
  return MembershipDuration(years, anniversary.daysUntil(today))
}

// TODO: Add the years and days to Lokalise as plurals.

/** The duration as "1 år och 245 dagar", leaving out a part that is zero, or null on the first day. */
internal fun MembershipDuration.toSwedishText(): String? {
  if (years == 0 && days == 0) return null
  val parts = buildList {
    if (years > 0) add("$years år")
    if (days > 0) add(if (days == 1) "1 dag" else "$days dagar")
  }
  return parts.joinToString(" och ")
}
