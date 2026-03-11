package com.hedvig.android.feature.terminateinsurance.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlinx.datetime.LocalDate
import org.junit.Test

class TerminationFlowComputationsTest {
  @Test
  fun `shouldDelete is true when masterInceptionDate is in the future`() {
    assertThat(
      TerminationFlowComputations.shouldDelete(
        masterInceptionDate = LocalDate(2026, 6, 1),
        today = LocalDate(2026, 3, 11),
      ),
    ).isTrue()
  }

  @Test
  fun `shouldDelete is false when masterInceptionDate is today or in the past`() {
    assertThat(
      TerminationFlowComputations.shouldDelete(
        masterInceptionDate = LocalDate(2026, 3, 11),
        today = LocalDate(2026, 3, 11),
      ),
    ).isFalse()
    assertThat(
      TerminationFlowComputations.shouldDelete(
        masterInceptionDate = LocalDate(2025, 1, 1),
        today = LocalDate(2026, 3, 11),
      ),
    ).isFalse()
  }

  @Test
  fun `minDate is masterInception plus 1 day when that is after today`() {
    assertThat(
      TerminationFlowComputations.minDate(
        masterInceptionDate = LocalDate(2026, 3, 11),
        today = LocalDate(2026, 3, 11),
      ),
    ).isEqualTo(LocalDate(2026, 3, 12))
  }

  @Test
  fun `minDate is today when masterInception plus 1 day is before today`() {
    assertThat(
      TerminationFlowComputations.minDate(
        masterInceptionDate = LocalDate(2020, 1, 1),
        today = LocalDate(2026, 3, 11),
      ),
    ).isEqualTo(LocalDate(2026, 3, 11))
  }

  @Test
  fun `maxDate is minDate plus 1 year`() {
    val min = LocalDate(2026, 3, 11)
    assertThat(TerminationFlowComputations.maxDate(min)).isEqualTo(LocalDate(2027, 3, 11))
  }

  // --- Car decom eligibility ---

  @Test
  fun `SE_CAR_FULL with commencement on cutoff date is eligible`() {
    assertThat(
      CarDecomEligibility.isEligible("SE_CAR_FULL", LocalDate(2026, 1, 15)),
    ).isTrue()
  }

  @Test
  fun `SE_CAR_HALF with commencement before cutoff is not eligible`() {
    assertThat(
      CarDecomEligibility.isEligible("SE_CAR_HALF", LocalDate(2026, 1, 14)),
    ).isFalse()
  }

  @Test
  fun `SE_CAR_TRAFFIC is never decom eligible`() {
    assertThat(
      CarDecomEligibility.isEligible("SE_CAR_TRAFFIC", LocalDate(2026, 6, 1)),
    ).isFalse()
  }

  @Test
  fun `null commencement date is not eligible`() {
    assertThat(
      CarDecomEligibility.isEligible("SE_CAR_FULL", null),
    ).isFalse()
  }

  // --- Car deflection routing ---

  @Test
  fun `SE_CAR_FULL with CAR_SOLD deflects to auto-cancel`() {
    val result = CarDeflectionRouter.route("SE_CAR_FULL", "CAR_SOLD", decomEligible = true)
    assertThat(result).isEqualTo(CarDeflectionRoute.AutoCancel)
  }

  @Test
  fun `SE_CAR_FULL with CAR_DECOMMISSIONED and decom eligible deflects to auto-decom`() {
    val result = CarDeflectionRouter.route("SE_CAR_FULL", "CAR_DECOMMISSIONED", decomEligible = true)
    assertThat(result).isEqualTo(CarDeflectionRoute.AutoDecommission)
  }

  @Test
  fun `SE_CAR_FULL with CAR_DECOMMISSIONED and NOT decom eligible deflects to auto-cancel`() {
    val result = CarDeflectionRouter.route("SE_CAR_FULL", "CAR_DECOMMISSIONED", decomEligible = false)
    assertThat(result).isEqualTo(CarDeflectionRoute.AutoCancel)
  }

  @Test
  fun `SE_CAR_TRAFFIC with CAR_SOLD deflects to auto-cancel`() {
    val result = CarDeflectionRouter.route("SE_CAR_TRAFFIC", "CAR_SOLD", decomEligible = false)
    assertThat(result).isEqualTo(CarDeflectionRoute.AutoCancel)
  }

  @Test
  fun `SE_CAR_DECOMMISSIONED with CAR_RECOMMISSIONED deflects to auto-decom (recommission)`() {
    val result = CarDeflectionRouter.route("SE_CAR_DECOMMISSIONED", "CAR_RECOMMISSIONED", decomEligible = false)
    assertThat(result).isEqualTo(CarDeflectionRoute.AutoDecommission)
  }

  @Test
  fun `non-car contract returns null`() {
    val result = CarDeflectionRouter.route("SE_APARTMENT_BRF", "MOVING", decomEligible = false)
    assertThat(result).isNull()
  }

  @Test
  fun `SE_CAR_FULL with BETTER_PRICE does not deflect`() {
    val result = CarDeflectionRouter.route("SE_CAR_FULL", "BETTER_PRICE", decomEligible = true)
    assertThat(result).isNull()
  }
}
