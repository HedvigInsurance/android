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
      val birthdate = convertSsnToBirthDateOrNull(ssn)
      CoInsuredPersonalInformation.EmptyInfo(birthdate)
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

  data class EmptyInfo(val dateOfBirth: LocalDate?) : CoInsuredPersonalInformation
}

private fun convertSsnToBirthDateOrNull(ssn: String): LocalDate? {
  val stringYear = ssn.substring(0, 4).toIntOrNull()
  val stringMonth = ssn.substring(4, 6).toIntOrNull()
  val stringDate = ssn.substring(6, 8).toIntOrNull()
  if (stringYear == null || stringMonth == null || stringDate == null) return null
  if (stringMonth !in 1..12) return null
  val result = try {
    LocalDate(year = stringYear, month = Month(stringMonth), dayOfMonth = stringDate)
  } catch (e: IllegalArgumentException) {
    null
  }
  return result
}
