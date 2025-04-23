package com.hedvig.android.feature.insurance.certificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.InsuranceEvidenceInitialDataQuery

internal interface GetInsuranceEvidenceInitialDataUseCase {
  suspend fun invoke(): Either<ErrorMessage, String>
}

internal class GetInsuranceEvidenceInitialDataUseCaseImpl(
  val apolloClient: ApolloClient,
) : GetInsuranceEvidenceInitialDataUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String> = either {
    val result = apolloClient.query(InsuranceEvidenceInitialDataQuery())
      .safeExecute(::ErrorMessage)
      .bind().currentMember.email
    result
  }
}
