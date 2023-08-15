package com.hedvig.android.data.travelcertificate

import app.cash.turbine.Turbine
import arrow.core.Either

class TestGetTravelCertificateSpecificationsUseCase : GetTravelCertificateSpecificationsUseCase {
  val turbine = Turbine<Either<TravelCertificateError, TravelCertificateData>>()

  override suspend fun invoke(): Either<TravelCertificateError, TravelCertificateData> {
    return turbine.awaitItem()
  }
}
