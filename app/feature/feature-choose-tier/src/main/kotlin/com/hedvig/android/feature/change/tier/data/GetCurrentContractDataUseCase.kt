package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import octopus.CurrentContractsForTierChangeQuery

internal interface GetCurrentContractDataUseCase {
  suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData>
}

internal class GetCurrentContractDataUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCurrentContractDataUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, CurrentContractData> {
    return either {
      val result = apolloClient
        .query(CurrentContractsForTierChangeQuery())
        .safeExecute()
        .mapLeft { ErrorMessage("Tried to start Change Tier flow but got error from CurrentContractsQuery") }
        .onLeft {
          logcat(ERROR) { "Tried to start Change Tier flow but got error from CurrentContractsQuery" }
        }
        .bind()
      val dataResult = result.currentMember.activeContracts.firstOrNull { it.id == insuranceId }
      if (dataResult == null) {
        logcat(ERROR) { "Tried to start Change Tier flow but got null active contract" }
        raise(ErrorMessage("Tried to start Change Tier flow but got null active contract"))
      } else {
        val agreement = CurrentContractData(dataResult.exposureDisplayName)
        agreement
      }
    }
  }
}

data class CurrentContractData(
  val currentExposureName: String,
)
