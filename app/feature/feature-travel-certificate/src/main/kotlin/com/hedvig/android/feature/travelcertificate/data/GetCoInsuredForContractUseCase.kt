package com.hedvig.android.feature.travelcertificate.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDate
import octopus.CoInsuredForContractQuery

interface GetCoInsuredForContractUseCase {
  fun invoke(contractId: String): Flow<Either<ErrorMessage, CoInsuredDataWithMember>>
}

internal class GetCoInsuredForContractUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCoInsuredForContractUseCase {
  override fun invoke(contractId: String): Flow<Either<ErrorMessage, CoInsuredDataWithMember>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient.query(CoInsuredForContractQuery(contractId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .safeFlow(::ErrorMessage)
            .map { rawData ->
              val result = either {
                val data = rawData.bind()
                val coInsured = data.contract.coInsured ?: listOf()
                val resultList = coInsured.map {
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
              result
            },
        )
        delay(1.seconds)
      }
    }
  }
}

data class CoInsuredDataWithMember(
  val coInsuredList: List<CoInsuredData>,
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
