package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceContractsQuery
import giraffe.type.TypeOfContract

internal interface GetInsuranceContractsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>>
}

internal class GetInsuranceContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : GetInsuranceContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>> {
    return either {
      val insuranceQueryData = apolloClient
        .query(InsuranceContractsQuery(languageService.getGraphQLLocale()))
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      insuranceQueryData.contracts.map(InsuranceContractsQuery.Contract::toContract)
    }
  }
}

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val statusPills: List<String>,
  val detailPills: List<String>,
  val isTerminated: Boolean,
  val typeOfContract: TypeOfContract,
)

private fun InsuranceContractsQuery.Contract.isTerminated(): Boolean {
  return this.status.fragments.contractStatusFragment.asTerminatedStatus != null
}

private fun InsuranceContractsQuery.Contract.toContract(): InsuranceContract {
  return InsuranceContract(
    id = id,
    displayName = displayName,
    statusPills = statusPills,
    detailPills = detailPills,
    isTerminated = isTerminated(),
    typeOfContract = typeOfContract,
  )
}
