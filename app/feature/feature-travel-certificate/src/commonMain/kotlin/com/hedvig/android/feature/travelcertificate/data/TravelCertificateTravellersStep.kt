package com.hedvig.android.feature.travelcertificate.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The input the travellers step starts from. Only `TravelCertificateDateInputPresenter` builds it, after it has
 * validated the email and checked the date against the allowed range. Public so iOS can hold it and pass it back;
 * the constructor and the properties are internal, so code outside this module cannot build or read it.
 */
@ConsistentCopyVisibility
@Serializable
public data class TravelCertificateTravellersStep internal constructor(
  internal val email: String,
  internal val travelDate: LocalDate,
  internal val contractId: String,
) {
  /** Leaves out [email]. */
  override fun toString(): String = "TravelCertificateTravellersStep(travelDate=$travelDate, contractId=$contractId)"
}
