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
import giraffe.GetUpcomingRenewalReminderQuery
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toKotlinLocalDate

interface GetUpcomingRenewalRemindersUseCase {
  suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>>
}

internal class GetUpcomingRenewalRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetUpcomingRenewalRemindersUseCase {
  override suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<UpcomingRenewal>> {
    return either {
      val contracts = apolloClient.query(GetUpcomingRenewalReminderQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(UpcomingRenewalReminderError::NetworkError)
        .bind()
        .contracts
      val upcomingRenewals: NonEmptyList<UpcomingRenewal>? = contracts
        .mapNotNull { contract ->
          if (contract.upcomingRenewal == null) return@mapNotNull null
          UpcomingRenewal(
            contract.displayName,
            contract.upcomingRenewal.renewalDate.toKotlinLocalDate(),
            contract.upcomingRenewal.draftCertificateUrl,
          )
        }
        .filter { upcomingRenewal ->
          val upcomingRenewalInstant: Instant =
            upcomingRenewal.renewalDate.atStartOfDayIn(TimeZone.currentSystemDefault())
          val currentInstant: Instant = clock.now()
          upcomingRenewalInstant > currentInstant
        }
        .toNonEmptyListOrNull()
      ensureNotNull(upcomingRenewals) {
        UpcomingRenewalReminderError.NoUpcomingRenewals
      }
      upcomingRenewals
    }
  }
}

sealed interface UpcomingRenewalReminderError {
  data object NoUpcomingRenewals : UpcomingRenewalReminderError
  data class NetworkError(val errorMessage: ErrorMessage) : UpcomingRenewalReminderError, ErrorMessage by errorMessage
}

data class UpcomingRenewal(
  val contractDisplayName: String,
  val renewalDate: LocalDate,
  val draftCertificateUrl: String,
)
