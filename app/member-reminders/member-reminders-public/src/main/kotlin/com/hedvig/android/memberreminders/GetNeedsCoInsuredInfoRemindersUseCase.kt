package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.NeedsCoInsuredInfoReminderQuery

internal interface GetNeedsCoInsuredInfoRemindersUseCase {
  suspend fun invoke(): Either<CoInsuredInfoReminderError, NonEmptyList<CoInsuredReminderInfo>>
}

internal class GetNeedsCoInsuredInfoRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetNeedsCoInsuredInfoRemindersUseCase {
  override suspend fun invoke(): Either<CoInsuredInfoReminderError, NonEmptyList<CoInsuredReminderInfo>> {
    return either {
      val contracts = apolloClient.query(NeedsCoInsuredInfoReminderQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(CoInsuredInfoReminderError::NetworkError)
        .bind()
        .currentMember
        .activeContracts

      val coInsuredReminderInfoList = contracts
        .filter { it.hasMissingInfo() }
        .map { CoInsuredReminderInfo(it.id) }
        .toNonEmptyListOrNull()

      ensureNotNull(coInsuredReminderInfoList) {
        CoInsuredInfoReminderError.NoCoInsuredReminders
      }
    }
  }

  private fun NeedsCoInsuredInfoReminderQuery.Data.CurrentMember.ActiveContract.hasMissingInfo() =
    (coInsured?.filter { it.hasMissingInfo }?.size ?: 0) > 0 || (coInsured?.filter { it.hasMissingInfo }?.size ?: 0) > 0
}

sealed interface CoInsuredInfoReminderError {
  data object NoCoInsuredReminders : CoInsuredInfoReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : CoInsuredInfoReminderError, ErrorMessage by errorMessage
}

data class CoInsuredReminderInfo(
  val contractId: String,
)
