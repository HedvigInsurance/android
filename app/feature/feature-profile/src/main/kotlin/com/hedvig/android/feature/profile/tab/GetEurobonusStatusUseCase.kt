package com.hedvig.android.feature.profile.tab

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.EurobonusDataQuery
import octopus.fragment.PartnerDataFragment

internal interface GetEurobonusStatusUseCase {
  suspend fun invoke(): Either<GetEurobonusError, EuroBonus>
}

internal class NetworkGetEurobonusStatusUseCase(
  private val apolloClient: ApolloClient,
) : GetEurobonusStatusUseCase {
  override suspend fun invoke(): Either<GetEurobonusError, EuroBonus> {
    return either {
      val result: PartnerDataFragment.PartnerData.Sas? = apolloClient.query(EurobonusDataQuery())
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .safeExecute(::ErrorMessage)
        .mapLeft(GetEurobonusError::Error)
        .bind()
        .currentMember
        .partnerData
        ?.sas
      ensure(result != null && result.eligible) {
        GetEurobonusError.EurobonusNotApplicable
      }
      EuroBonus(result.eurobonusNumber)
    }
  }
}

internal sealed interface GetEurobonusError {
  object EurobonusNotApplicable : GetEurobonusError {
    override fun toString() = "EurobonusNotApplicable"
  }

  data class Error(
    val errorMessage: ErrorMessage,
  ) : GetEurobonusError, ErrorMessage by errorMessage
}

internal data class EuroBonus(
  val code: String?,
)
