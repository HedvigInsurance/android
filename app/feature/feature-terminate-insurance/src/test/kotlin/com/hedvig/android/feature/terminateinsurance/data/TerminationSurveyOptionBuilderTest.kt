package com.hedvig.android.feature.terminateinsurance.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import org.junit.Test

class TerminationSurveyOptionBuilderTest {

  @Test
  fun `swedish apartment BRF builds correct base and home options without tier support`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val optionIds = options.map { it.id }
    assertThat(optionIds).contains("BETTER_PRICE")
    assertThat(optionIds).contains("MISSING_COVERAGE_OR_TERMS")
    assertThat(optionIds).contains("DISSATISFIED_SERVICE")
    assertThat(optionIds).contains("OTHER_REASON")
    assertThat(optionIds).contains("MOVING")
    assertThat(optionIds).contains("NO_LONGER_NEEDED")

    val moving = options.first { it.id == "MOVING" }
    assertThat(moving.subOptions.map { it.id }).isEqualTo(
      listOf("MOVING_NEW_ADDRESS", "MOVED_IN_WITH_SOMEONE", "MOVING_CANCEL_WITHOUT_NEW_QUOTE"),
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    val nlnIds = noLongerNeeded.subOptions.map { it.id }
    assertThat(nlnIds).contains("MOVED_ABROAD")
    assertThat(nlnIds).doesNotContain("MOVED_ABROAD_CHANGE_TIER")
  }

  @Test
  fun `swedish apartment BRF with tier support adds sub-options to BETTER_PRICE and uses MOVED_ABROAD_CHANGE_TIER`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF",
      supportsBetterPrice = true,
      supportsBetterCoverage = true,
      memberId = "123",
    )

    val betterPrice = options.first { it.id == "BETTER_PRICE" }
    assertThat(betterPrice.subOptions.map { it.id }).isEqualTo(
      listOf("BETTER_PRICE_CHANGE_COVERAGE_LEVEL", "BETTER_PRICE_CANCEL_WITHOUT_NEW_QUOTE"),
    )

    val missingCoverage = options.first { it.id == "MISSING_COVERAGE_OR_TERMS" }
    assertThat(missingCoverage.subOptions.map { it.id }).isEqualTo(
      listOf("MISSING_COVERAGE_OR_TERMS_CHANGE_COVERAGE_LEVEL", "MISSING_COVERAGE_OR_TERMS_CANCEL_WITHOUT_NEW_QUOTE"),
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    val nlnIds = noLongerNeeded.subOptions.map { it.id }
    assertThat(nlnIds).contains("MOVED_ABROAD_CHANGE_TIER")
    assertThat(nlnIds).doesNotContain("MOVED_ABROAD")
  }

  @Test
  fun `MAX home variants use MOVED_ABROAD without change tier`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF_MAX",
      supportsBetterPrice = true,
      supportsBetterCoverage = true,
      memberId = "123",
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    val nlnIds = noLongerNeeded.subOptions.map { it.id }
    assertThat(nlnIds).contains("MOVED_ABROAD")
    assertThat(nlnIds).doesNotContain("MOVED_ABROAD_CHANGE_TIER")
  }

  @Test
  fun `student apartment uses MOVED_IN_WITH_SOMEONE_STUDENT`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_STUDENT_BRF",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val moving = options.first { it.id == "MOVING" }
    assertThat(moving.subOptions.map { it.id }).contains("MOVED_IN_WITH_SOMEONE_STUDENT")
  }

  @Test
  fun `car contract builds correct NO_LONGER_NEEDED sub-options`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_CAR_FULL",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    assertThat(noLongerNeeded.subOptions.map { it.id }).isEqualTo(
      listOf("CAR_SOLD", "CAR_DECOMMISSIONED", "CAR_SCRAPPED", "CAR_OTHER"),
    )
  }

  @Test
  fun `decommissioned car has CAR_RECOMMISSIONED and forces MISSING_COVERAGE sub-options`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_CAR_DECOMMISSIONED",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    assertThat(noLongerNeeded.subOptions.map { it.id }).contains("CAR_RECOMMISSIONED")
    assertThat(noLongerNeeded.subOptions.map { it.id }).doesNotContain("CAR_DECOMMISSIONED")

    val missingCoverage = options.first { it.id == "MISSING_COVERAGE_OR_TERMS" }
    assertThat(missingCoverage.subOptions).hasSize(2)
  }

  @Test
  fun `pet contract builds correct sub-options`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_DOG_STANDARD",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    assertThat(noLongerNeeded.subOptions.map { it.id }).isEqualTo(
      listOf("PET_NEW_OWNER", "PET_NO_LONGER_LIVES", "PET_OTHER"),
    )
  }

  @Test
  fun `accident contract adds ACCIDENT_OTHER`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_ACCIDENT",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    assertThat(options.map { it.id }).contains("ACCIDENT_OTHER")
  }

  @Test
  fun `vacation home has SOLD_VACATION_HOME and NO_LONGER_NEEDED_OTHER`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_VACATION_HOME_BAS",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val noLongerNeeded = options.first { it.id == "NO_LONGER_NEEDED" }
    assertThat(noLongerNeeded.subOptions.map { it.id }).isEqualTo(
      listOf("SOLD_VACATION_HOME", "NO_LONGER_NEEDED_OTHER"),
    )
  }

  @Test
  fun `shuffling places other options last`() {
    val options = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "123",
    )

    val lastOption = options.last()
    assertThat(lastOption.id).isEqualTo("OTHER_REASON")
  }

  @Test
  fun `shuffling is deterministic for same member ID`() {
    val options1 = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "abc",
    )
    val options2 = TerminationSurveyOptionBuilder.build(
      typeOfContract = "SE_APARTMENT_BRF",
      supportsBetterPrice = false,
      supportsBetterCoverage = false,
      memberId = "abc",
    )
    assertThat(options1.map { it.id }).isEqualTo(options2.map { it.id })
  }
}
