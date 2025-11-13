package com.hedvig.android.feature.claimhistory

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.ClaimsHistoryQuery
import octopus.type.ClaimOutcome

internal class GetClaimsHistoryUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(): Flow<Either<ErrorMessage, List<ClaimHistory>>> {
    return apolloClient
      .query(ClaimsHistoryQuery())
      .safeFlow(::ErrorMessage)
      .map { result ->
        either {
          val commonList = result
            .bind()
            .currentMember
            .claimsHistory
            .map { history ->
              ClaimHistory(
                id = history.id,
                claimType = history.claimType,
                submittedAt = history.submittedAt,
                outcome = when (history.outcome) {
                  ClaimOutcome.PAID -> ClaimHistory.ClaimOutcome.PAID
                  ClaimOutcome.NOT_COMPENSATED -> ClaimHistory.ClaimOutcome.NOT_COMPENSATED
                  ClaimOutcome.NOT_COVERED -> ClaimHistory.ClaimOutcome.NOT_COVERED
                  ClaimOutcome.UNRESPONSIVE -> ClaimHistory.ClaimOutcome.UNRESPONSIVE
                  ClaimOutcome.UNKNOWN__,
                  null,
                  -> ClaimHistory.ClaimOutcome.UNKNOWN
                },
              )
            } +
            result
              .bind()
              .currentMember
              .partnerClaimsHistory
              .map { history ->
                ClaimHistory(
                  id = history.id,
                  claimType = history.claimType,
                  submittedAt = history.submittedAt.atStartOfDayIn(TimeZone.UTC), //todo: which TimeZone???
                  outcome = ClaimHistory.ClaimOutcome.UNKNOWN
                )
              }
          commonList.sortedByDescending { it.submittedAt }
        }
      }
  }
}

internal data class ClaimHistory(
  val id: String,
  // Title, of fall back to "Claim"
  val claimType: String?,
  // Subtitle uses this date to show when the claim was submitted
  val submittedAt: Instant,
  val outcome: ClaimOutcome?,
) {
  enum class ClaimOutcome {
    PAID,
    NOT_COMPENSATED,
    NOT_COVERED,
    UNRESPONSIVE,
    UNKNOWN,
  }
}
