package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeWatch
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
  private val featureManager: FeatureManager,
) : GetClaimRatingRemindersUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, List<MemberReminder.ClaimRatingPending>>> {
    return featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_RATING).flatMapLatest { isEnabled ->
      if (!isEnabled) {
        return@flatMapLatest flowOf(emptyList<MemberReminder.ClaimRatingPending>().right())
      }
      apolloClient.query(ClaimRatingRemindersQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeWatch()
        .map { result ->
          // Submitting a rating writes hasSubmittedRating back to the cached claim, so a rated claim drops out
          // here without refetching the list.
          result.mapLeft(::ErrorMessage).map { data ->
            data.currentMember.claimsPendingRating
              .filterNot { it.hasSubmittedRating }
              .map { claim -> MemberReminder.ClaimRatingPending(claimId = claim.id) }
          }
        }
    }
  }
}
