package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal interface CheckCertificatesAvailabilityUseCase {
  suspend fun invoke(): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class CheckCertificatesAvailabilityUseCaseImpl(
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
  private val checkGreenCardDestinationAvailabilityUseCase: CheckGreenCardDestinationAvailabilityUseCase,
) : CheckCertificatesAvailabilityUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    val (insuranceEvidence, travelCertificate, greenCard) = combine(
      flow { emit(checkInsuranceEvidenceAvailabilityUseCase.invoke()) },
      flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
      flow { emit(checkGreenCardDestinationAvailabilityUseCase.invoke()) },
    ) { insuranceEvidence, travelCertificate, greenCard ->
      Triple(insuranceEvidence, travelCertificate, greenCard)
    }.first()
    return either {
      if (insuranceEvidence.isLeft() && travelCertificate.isLeft() && greenCard.isLeft()) {
        logcat { "CheckCertificatesAvailabilityUseCase: no certificates available" }
        raise(ErrorMessage())
      } else {
        logcat { "CheckCertificatesAvailabilityUseCase: certificates available" }
      }
    }
  }
}
