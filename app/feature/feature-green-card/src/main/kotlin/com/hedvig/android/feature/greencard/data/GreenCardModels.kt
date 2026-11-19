package com.hedvig.android.feature.greencard.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

internal data class GreenCard(
  val id: String,
  val signedUrl: String,
  val registrationNumber: String,
  val validFrom: LocalDate,
  val validTo: LocalDate,
  val isExpiredNow: Boolean,
)

internal data class GreenCardSpecifications(
  val contracts: List<GreenCardContractSpecification>,
)

internal data class GreenCardContractSpecification(
  val contractId: String,
  val registrationNumber: String,
  val minStartDate: LocalDate,
  /**
   * The day the contract terminates, or null when it is not terminating. Also the latest possible departure date.
   */
  val maxEndDate: LocalDate?,
  val maxPeriodDays: Int,
) {
  fun latestEndDateFor(startDate: LocalDate): LocalDate {
    val endOfPeriod = startDate.plus(maxPeriodDays, DateTimeUnit.DAY)
    return if (maxEndDate == null) endOfPeriod else minOf(maxEndDate, endOfPeriod)
  }
}

@Serializable
internal data class GreenCardAddress(
  val street: String,
  val postalCode: String,
  val city: String,
  val country: Country,
)

internal data class GreenCardCoDriver(
  val name: String,
  val address: GreenCardAddress,
)

@JvmInline
@Serializable
internal value class GreenCardUrl(val uri: String)
