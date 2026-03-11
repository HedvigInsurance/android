package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal object TerminationFlowComputations {
  fun shouldDelete(masterInceptionDate: LocalDate, today: LocalDate): Boolean =
    masterInceptionDate > today

  fun minDate(masterInceptionDate: LocalDate, today: LocalDate): LocalDate =
    maxOf(masterInceptionDate.plus(1, DateTimeUnit.DAY), today)

  fun maxDate(minDate: LocalDate): LocalDate =
    minDate.plus(1, DateTimeUnit.YEAR)
}

internal enum class CarDeflectionRoute {
  AutoCancel,
  AutoDecommission,
}

/**
 * Determines car-specific deflection routing based on contract type, selected survey option, and decom eligibility.
 * Returns null if no deflection applies (proceed to date/deletion step).
 *
 * Mirrors Odyssey's TerminationCarDeflectAutoCancel and TerminationCarAutoDecom step isApplicable logic.
 */
internal object CarDeflectionRouter {
  private val AUTO_CANCEL_OPTIONS = setOf("CAR_SOLD", "CAR_SCRAPPED")

  fun route(typeOfContract: String, selectedOptionId: String, decomEligible: Boolean): CarDeflectionRoute? {
    return when (typeOfContract) {
      "SE_CAR_TRAFFIC", "SE_CAR_TRIAL_HALF", "SE_CAR_TRIAL_FULL" -> {
        if (selectedOptionId in AUTO_CANCEL_OPTIONS || selectedOptionId == "CAR_DECOMMISSIONED") {
          CarDeflectionRoute.AutoCancel
        } else {
          null
        }
      }

      "SE_CAR_DECOMMISSIONED" -> {
        when (selectedOptionId) {
          "CAR_SOLD", "CAR_SCRAPPED" -> CarDeflectionRoute.AutoCancel
          "CAR_RECOMMISSIONED" -> CarDeflectionRoute.AutoDecommission
          else -> null
        }
      }

      "SE_CAR_HALF", "SE_CAR_FULL" -> {
        when {
          selectedOptionId in AUTO_CANCEL_OPTIONS -> CarDeflectionRoute.AutoCancel
          selectedOptionId == "CAR_DECOMMISSIONED" && decomEligible -> CarDeflectionRoute.AutoDecommission
          selectedOptionId == "CAR_DECOMMISSIONED" && !decomEligible -> CarDeflectionRoute.AutoCancel
          else -> null
        }
      }

      else -> null
    }
  }
}
