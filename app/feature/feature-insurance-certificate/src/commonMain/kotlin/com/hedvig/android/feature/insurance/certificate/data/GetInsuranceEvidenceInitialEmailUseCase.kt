package com.hedvig.android.feature.insurance.certificate.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InsuranceEvidenceInitialDataQuery

internal interface GetInsuranceEvidenceInitialEmailUseCase {
  suspend fun invoke(): Either<ErrorMessage, String>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInsuranceEvidenceInitialEmailUseCaseImpl(
  val apolloClient: ApolloClient,
) : GetInsuranceEvidenceInitialEmailUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String> = either {
    val result = apolloClient.query(InsuranceEvidenceInitialDataQuery())
      .safeExecute(::ErrorMessage)
      .bind()
    ensure(result.currentMember.memberActions?.isCreatingOfInsuranceEvidenceEnabled == true) {
      logcat { "GetInsuranceEvidenceInitialDataUseCase: creating InsuranceEvidence is not enabled" }
      ErrorMessage("isCreatingOfInsuranceEvidenceEnabled is false")
    }
    val email = result.currentMember.email
    email
  }
}
