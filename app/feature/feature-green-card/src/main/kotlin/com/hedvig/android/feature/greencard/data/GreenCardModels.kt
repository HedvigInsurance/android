package com.hedvig.android.feature.greencard.data

import kotlinx.datetime.LocalDate
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
  val email: String?,
  val contracts: List<GreenCardContractSpecification>,
)

internal data class GreenCardContractSpecification(
  val contractId: String,
  val registrationNumber: String,
  val minStartDate: LocalDate,
  val maxEndDate: LocalDate,
)

@JvmInline
@Serializable
internal value class GreenCardUrl(val uri: String)
