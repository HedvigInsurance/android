package com.hedvig.app.feature.profile.ui.tab

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.EurobonusDataQuery
import octopus.fragment.PartnerDataFragment

internal interface GetEuroBonusStatusUseCase {
  suspend fun invoke(): Either<GetEuroBonusError, EuroBonus>
}

internal class NetworkGetEuroBonusStatusUseCase(
  private val apolloClient: ApolloClient,
) : GetEuroBonusStatusUseCase {
  override suspend fun invoke(): Either<GetEuroBonusError, EuroBonus> {
    return either {
      val result: PartnerDataFragment.PartnerData.Sas? = apolloClient.query(EurobonusDataQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(GetEuroBonusError::Error)
        .bind()
        .currentMember
        .partnerData
        ?.sas
      ensure(result != null && result.eligible) {
        GetEuroBonusError.EuroBonusNotApplicable
      }
      EuroBonus(result.eurobonusNumber)
    }
  }
}

internal sealed interface GetEuroBonusError {
  object EuroBonusNotApplicable : GetEuroBonusError
  data class Error(
    val errorMessage: ErrorMessage,
  ) : GetEuroBonusError, ErrorMessage by errorMessage
}

internal data class EuroBonus(
  val code: String?,
)
