package com.hedvig.android.data.travelcertificate

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate
import octopus.CoEnsuredForContractQuery

interface GetCoEnsuredForContractUseCase {
  suspend fun invoke(contractId: String): Either<ErrorMessage, CoInsuredDataWithMember>
}

internal class GetCoEnsuredForContractUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCoEnsuredForContractUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, CoInsuredDataWithMember> {
    return apolloClient.query(CoEnsuredForContractQuery(contractId))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { data ->
        val coEnsured = data.contract.coInsured ?: listOf()
        val resultList = coEnsured.map {
          CoInsuredData(
            firstName = it.firstName,
            lastName = it.lastName,
            ssn = it.ssn,
            dateOfBirth = it.birthdate,
            hasMissingInfo = it.hasMissingInfo,
            id = it.id,
          )
        }
        CoInsuredDataWithMember(
          resultList,
          "${data.currentMember.firstName} ${data.currentMember.lastName}",
        )
      }
  }
}

data class CoInsuredDataWithMember(
  val coEnsuredList: List<CoInsuredData>,
  val memberFullName: String,
)

data class CoInsuredData(
  val id: String?,
  val firstName: String?,
  val lastName: String?,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val hasMissingInfo: Boolean,
)
