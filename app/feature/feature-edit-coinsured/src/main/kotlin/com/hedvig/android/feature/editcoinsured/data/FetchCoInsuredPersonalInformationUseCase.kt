package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
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
    if (result.personalInformation == null) {
      CoInsuredPersonalInformation.EmptyInfo(convertSsnToBirthDate(ssn))
    } else {
      CoInsuredPersonalInformation.FullInfo(
        firstName = result.personalInformation.firstName,
        lastName = result.personalInformation.lastName,
      )
    }
  }
}

internal interface CoInsuredPersonalInformation {
  data class FullInfo(
    val firstName: String,
    val lastName: String,
  ) : CoInsuredPersonalInformation

  data class EmptyInfo(val dateOfBirth: LocalDate) : CoInsuredPersonalInformation
}

private fun convertSsnToBirthDate(ssn: String): LocalDate {
  val stringYear = ssn.substring(0, 4).toInt()
  val stringMonth = ssn.substring(4, 6).toInt()
  val stringDate = ssn.substring(6, 8).toInt()
  return LocalDate(year = stringYear, month = Month(stringMonth), dayOfMonth = stringDate)
}
