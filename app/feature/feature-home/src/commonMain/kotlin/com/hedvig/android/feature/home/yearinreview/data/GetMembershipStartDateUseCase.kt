package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.YearInReviewMembershipQuery

internal interface GetMembershipStartDateUseCase {
  /**
   * The day the member's first insurance with Hedvig started, counting ended insurances too, or null when
   * none has started yet.
   */
  suspend fun invoke(): Either<ErrorMessage, LocalDate?>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetMembershipStartDateUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
  private val timeZone: TimeZone,
) : GetMembershipStartDateUseCase {
  override suspend fun invoke(): Either<ErrorMessage, LocalDate?> {
    return apolloClient.query(YearInReviewMembershipQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute()
      .onLeft { error -> logcat(operationError = error) { "YearInReviewMembershipQuery failed with $error" } }
      .mapLeft { ErrorMessage() }
      .map { data ->
        val today = clock.now().toLocalDateTime(timeZone).date
        val contracts = data.currentMember.activeContracts.map { it.masterInceptionDate } +
          data.currentMember.terminatedContracts.map { it.masterInceptionDate }
        contracts.filter { it <= today }.minOrNull()
      }
  }
}
