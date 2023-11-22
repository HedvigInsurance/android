package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.PersonalInformationQuery

interface FetchCoInsuredPersonalInformationUseCase {
  suspend fun invoke(ssn: String): Either<ErrorMessage, CoInsured>
}

class FetchCoInsuredPersonalInformationUseCaseImpl(
  private val apolloClient: ApolloClient,
) : FetchCoInsuredPersonalInformationUseCase {
  override suspend fun invoke(ssn: String): Either<ErrorMessage, CoInsured> = either {
    val result = apolloClient.query(PersonalInformationQuery(ssn))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    ensureNotNull(result.personalInformation) {
      ErrorMessage("No personal information found")
    }

    CoInsured(
      firstName = result.personalInformation.firstName,
      lastName = result.personalInformation.lastName,
      birthDate = null,
      ssn = ssn,
      hasMissingInfo = false,
    )
  }
}

data class CoInsuredPersonalInformationError(val message: String?)
