package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

interface CheckCertificatesAvailabilityUseCase {
  suspend fun invoke(): Either<ErrorMessage, Unit>
}

internal class CheckCertificatesAvailabilityUseCaseImpl(
  private val checkTravelCertificateDestinationAvailabilityUseCase: CheckTravelCertificateDestinationAvailabilityUseCase,
  private val checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
) : CheckCertificatesAvailabilityUseCase {

  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    val combinedResult = combine(
      flow { emit(checkInsuranceEvidenceAvailabilityUseCase.invoke()) },
      flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
    ) { insuranceEvidence, travelCertificateDestination ->
      insuranceEvidence to travelCertificateDestination
    }.first()
    return either {
      val (insuranceEvidenceAvailability, travelCertificateDestinationAvailability) = combinedResult
      val evidenceAvailable = insuranceEvidenceAvailability.getOrNull()
      val travelCertificateDestinationAvailable = travelCertificateDestinationAvailability.getOrNull()
      if (evidenceAvailable == null && travelCertificateDestinationAvailable == null) {
        logcat { "CheckCertificatesAvailabilityUseCase: no certificates available" }
        raise(ErrorMessage())
      } else {
        logcat { "CheckCertificatesAvailabilityUseCase: certificates available" }
      }
    }
  }
}
