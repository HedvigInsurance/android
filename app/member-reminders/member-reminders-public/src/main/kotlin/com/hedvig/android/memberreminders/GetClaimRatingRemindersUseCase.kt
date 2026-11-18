package com.hedvig.android.memberreminders

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeWatch
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.ClaimRatingRemindersQuery

internal interface GetClaimRatingRemindersUseCase {
  fun invoke(): Flow<Either<ErrorMessage, List<MemberReminder.ClaimRatingPending>>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetClaimRatingRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetClaimRatingRemindersUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, List<MemberReminder.ClaimRatingPending>>> {
    return apolloClient.query(ClaimRatingRemindersQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeWatch()
      .map { result ->
        // The rating mutations write isRatingPending back to the cached claim, so an answered claim drops out here
        // without refetching the list.
        result.mapLeft(::ErrorMessage).map { data ->
          data.currentMember.claimsPendingRating
            .filter { it.isRatingPending }
            .map { claim -> MemberReminder.ClaimRatingPending(claimId = claim.id) }
        }
      }
  }
}
