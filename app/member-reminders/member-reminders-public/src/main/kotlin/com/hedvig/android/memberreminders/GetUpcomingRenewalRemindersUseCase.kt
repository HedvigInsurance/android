package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.GetUpcomingRenewalReminderQuery
import octopus.type.AgreementCreationCause

internal interface GetUpcomingRenewalRemindersUseCase {
  suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<MemberReminder.UpcomingRenewal>>
}

internal class GetUpcomingRenewalRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetUpcomingRenewalRemindersUseCase {
  override suspend fun invoke(): Either<UpcomingRenewalReminderError, NonEmptyList<MemberReminder.UpcomingRenewal>> {
    return either {
      val contracts = apolloClient.query(GetUpcomingRenewalReminderQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(UpcomingRenewalReminderError::NetworkError)
        .bind()
        .currentMember
        .activeContracts

      val upcomingRenewals: NonEmptyList<MemberReminder.UpcomingRenewal>? = contracts
        .filter { it.upcomingChangedAgreement?.creationCause == AgreementCreationCause.RENEWAL }
        .mapNotNull { contract ->
          val upcomingChangedAgreement = contract.upcomingChangedAgreement ?: return@mapNotNull null
          MemberReminder.UpcomingRenewal(
            contract.currentAgreement.productVariant.displayName,
            upcomingChangedAgreement.activeFrom,
            upcomingChangedAgreement.certificateUrl,
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
