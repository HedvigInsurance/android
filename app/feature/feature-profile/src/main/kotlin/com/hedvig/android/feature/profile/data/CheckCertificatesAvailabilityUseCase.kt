package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

interface CheckCertificatesAvailabilityUseCase {
  suspend fun invoke(): Either<ErrorMessage, Unit>
}

internal class CheckCertificatesAvailabilityUseCaseImpl(
  private val checkTravelCertificateDestinationAvailabilityUseCase: CheckTravelCertificateDestinationAvailabilityUseCase,
  private val checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
) : CheckCertificatesAvailabilityUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    return either {
      combine(
        flow { emit(checkInsuranceEvidenceAvailabilityUseCase.invoke()) },
        flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
      ) { insuranceEvidenceAvailability, travelCertificateDestinationAvailability ->
        val evidenceAvailable = insuranceEvidenceAvailability.getOrNull()
        val travelCertificateDestinationAvailable = travelCertificateDestinationAvailability.getOrNull()
        if (evidenceAvailable == null && travelCertificateDestinationAvailable == null) {
          raise(ErrorMessage())
        }
      }
    }
  }
}
