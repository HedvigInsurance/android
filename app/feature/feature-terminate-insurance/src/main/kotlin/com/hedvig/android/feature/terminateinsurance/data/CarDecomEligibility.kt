package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.LocalDate

internal object CarDecomEligibility {
  private val CUTOFF_DATE = LocalDate(2026, 1, 15)
  private val ELIGIBLE_TYPES = setOf("SE_CAR_HALF", "SE_CAR_FULL")

  fun isEligible(typeOfContract: String, commencementDate: LocalDate?): Boolean {
    if (typeOfContract !in ELIGIBLE_TYPES) return false
    if (commencementDate == null) return false
    return commencementDate >= CUTOFF_DATE
  }
}
