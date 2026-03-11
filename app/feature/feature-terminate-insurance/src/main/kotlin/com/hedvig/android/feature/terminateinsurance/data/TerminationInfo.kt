package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.LocalDate

/**
 * All data needed for the client-side termination flow, fetched from one federated query.
 */
internal data class TerminationInfo(
  val contractId: String,
  val masterInceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val existingAddons: List<ExtraCoverageItem>,
  val supportsBetterPrice: Boolean,
  val supportsBetterCoverage: Boolean,
  val typeOfContract: String,
  val commencementDate: LocalDate?,
)
