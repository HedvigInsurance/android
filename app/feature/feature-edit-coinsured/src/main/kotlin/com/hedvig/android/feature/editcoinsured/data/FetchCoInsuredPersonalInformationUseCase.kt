package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.PersonalInformationQuery

internal interface FetchCoInsuredPersonalInformationUseCase {
  suspend fun invoke(ssn: String): Either<ErrorMessage, CoInsuredPersonalInformation>
}

internal class FetchCoInsuredPersonalInformationUseCaseImpl(
  private val apolloClient: ApolloClient,
) : FetchCoInsuredPersonalInformationUseCase {
  override suspend fun invoke(ssn: String): Either<ErrorMessage, CoInsuredPersonalInformation> = either {
    val result = apolloClient.query(PersonalInformationQuery(ssn))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    ensureNotNull(result.personalInformation) {
      // todo localize this error message. Perhaps trigger "manual input" directly when we get this error too.
      ErrorMessage("No personal information found")
    }

    CoInsuredPersonalInformation(
      firstName = result.personalInformation.firstName,
      lastName = result.personalInformation.lastName,
    )
  }
}

internal data class CoInsuredPersonalInformation(
  val firstName: String,
  val lastName: String,
)
