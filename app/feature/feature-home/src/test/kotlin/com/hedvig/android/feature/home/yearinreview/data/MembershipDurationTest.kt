package com.hedvig.android.feature.home.yearinreview.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.datetime.LocalDate
import org.junit.Test

internal class MembershipDurationTest {
  @Test
  fun `counts whole years and the days left over`() {
    val duration = membershipDuration(start = LocalDate(2024, 3, 1), today = LocalDate(2025, 11, 1))

    assertThat(duration).isEqualTo(MembershipDuration(years = 1, days = 245))
  }

  @Test
  fun `a membership started on a leap day completes its year on the first of March`() {
    assertThat(membershipDuration(start = LocalDate(2024, 2, 29), today = LocalDate(2025, 2, 28)))
      .isEqualTo(MembershipDuration(years = 0, days = 365))
    assertThat(membershipDuration(start = LocalDate(2024, 2, 29), today = LocalDate(2025, 3, 1)))
      .isEqualTo(MembershipDuration(years = 1, days = 0))
  }

  @Test
  fun `under a year counts only days`() {
    val duration = membershipDuration(start = LocalDate(2025, 10, 31), today = LocalDate(2025, 11, 1))

    assertThat(duration).isEqualTo(MembershipDuration(years = 0, days = 1))
  }

  @Test
  fun `the text leaves out a zero part and uses the singular for one day`() {
    assertThat(MembershipDuration(years = 1, days = 245).toSwedishText()).isEqualTo("1 år och 245 dagar")
    assertThat(MembershipDuration(years = 3, days = 0).toSwedishText()).isEqualTo("3 år")
    assertThat(MembershipDuration(years = 0, days = 1).toSwedishText()).isEqualTo("1 dag")
    assertThat(MembershipDuration(years = 0, days = 0).toSwedishText()).isNull()
  }
}
