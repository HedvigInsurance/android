package com.hedvig.android.feature.travelcertificate.ios

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import kotlinx.datetime.LocalDate

/** A certificate that exists now. [certificateUrl] is signed: it opens the PDF without signing in. */
data class TravelCertificateCreated(val certificateUrl: String) {
  /** Leaves out [certificateUrl]. */
  override fun toString(): String = "TravelCertificateCreated(certificateUrl=redacted)"
}

internal fun TravelCertificateUrl.toCreated(): TravelCertificateCreated = TravelCertificateCreated(uri)

/** The calendar day as `yyyy-MM-dd`. */
internal fun LocalDate.toIsoDateString(): String = LocalDate.Formats.ISO.format(this)

/** The day [isoDate] names as `yyyy-MM-dd`, or null for any other text and for a day that does not exist. */
internal fun parseIsoDateOrNull(isoDate: String): LocalDate? = LocalDate.Formats.ISO.parseOrNull(isoDate)
