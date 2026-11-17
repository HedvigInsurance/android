package com.hedvig.android.feature.travelcertificate.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The input the travellers step starts from. Only `TravelCertificateDateInputPresenter` builds it, after it has
 * validated the email and checked the date against the allowed range.
 */
@Serializable
internal data class TravelCertificateTravellersStep(
  val email: String,
  val travelDate: LocalDate,
  val contractId: String,
)
