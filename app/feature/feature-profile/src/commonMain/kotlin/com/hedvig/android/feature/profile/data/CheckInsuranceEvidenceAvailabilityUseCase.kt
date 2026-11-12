package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InsuranceEvidenceAvailabilityQuery

internal interface CheckInsuranceEvidenceAvailabilityUseCase {
  suspend fun invoke(): Either<InsuranceEvidenceAvailabilityError, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class CheckInsuranceEvidenceAvailabilityUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CheckInsuranceEvidenceAvailabilityUseCase {
  override suspend fun invoke(): Either<InsuranceEvidenceAvailabilityError, Unit> {
    return either {
      val isInsuranceEvidenceEnabledResult = apolloClient.query(InsuranceEvidenceAvailabilityQuery())
        .safeExecute {
          ErrorMessage("Could not check isCreatingOfInsuranceEvidenceEnabled: $it")
        }
        .map {
          it.currentMember.memberActions?.isCreatingOfInsuranceEvidenceEnabled
        }
        .onLeft { errorMessage ->
          logcat(
            priority = LogPriority.ERROR,
          ) { "CheckInsuranceEvidenceAvailability error. Message: ${errorMessage.message}" }
          raise(InsuranceEvidenceAvailabilityError.Error(errorMessage))
        }
      val isInsuranceEvidenceEnabled = isInsuranceEvidenceEnabledResult.getOrNull() == true
      ensure(isInsuranceEvidenceEnabled) {
        InsuranceEvidenceAvailabilityError.InsuranceEvidenceNotAvailable
      }
    }
  }
}

sealed interface InsuranceEvidenceAvailabilityError {
  data object InsuranceEvidenceNotAvailable : InsuranceEvidenceAvailabilityError

  data class Error(
    val errorMessage: ErrorMessage,
  ) : InsuranceEvidenceAvailabilityError, ErrorMessage by errorMessage
}
