package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.GetUpcomingRenewalReminderQuery
import octopus.type.AgreementCreationCause

internal interface GetUpcomingRenewalRemindersUseCase {
  fun invoke(): Flow<Either<UpcomingRenewalReminderError, NonEmptyList<MemberReminder.UpcomingRenewal>>>
}

internal class GetUpcomingRenewalRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetUpcomingRenewalRemindersUseCase {
  override fun invoke(): Flow<Either<UpcomingRenewalReminderError, NonEmptyList<MemberReminder.UpcomingRenewal>>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient.query(GetUpcomingRenewalReminderQuery())
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .safeFlow(::ErrorMessage)
            .map { result ->
              either {
                val contracts = result.mapLeft(UpcomingRenewalReminderError::NetworkError)
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
            },
        )
        delay(60.seconds)
      }
    }
  }
}

sealed interface UpcomingRenewalReminderError {
  data object NoUpcomingRenewals : UpcomingRenewalReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : UpcomingRenewalReminderError, ErrorMessage by errorMessage
}
