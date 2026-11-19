package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.ClaimDismissRatingMutation
import octopus.ClaimSubmitRatingMutation

internal sealed interface ClaimRatingAnswer {
  data class Rated(val rating: Int, val comment: String?) : ClaimRatingAnswer

  data object Dismissed : ClaimRatingAnswer
}

internal interface SubmitClaimRatingUseCase {
  suspend fun invoke(claimId: String, rating: Int, comment: String?): Either<ErrorMessage, Unit>
}

internal interface DismissClaimRatingUseCase {
  suspend fun invoke(claimId: String): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@Inject
internal class SubmitClaimRatingUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitClaimRatingUseCase {
  override suspend fun invoke(claimId: String, rating: Int, comment: String?): Either<ErrorMessage, Unit> {
    return either {
      val data = apolloClient
        .mutation(ClaimSubmitRatingMutation(claimId, rating, Optional.presentIfNotNull(comment)))
        .safeExecute(::ErrorMessage)
        .bind()
      data.claimSubmitRating?.userError?.let { raise(ErrorMessage(it.message)) }
    }
  }
}

@ContributesBinding(AppScope::class)
@Inject
internal class DismissClaimRatingUseCaseImpl(
  private val apolloClient: ApolloClient,
) : DismissClaimRatingUseCase {
  override suspend fun invoke(claimId: String): Either<ErrorMessage, Unit> {
    return either {
      val data = apolloClient
        .mutation(ClaimDismissRatingMutation(claimId))
        .safeExecute(::ErrorMessage)
        .bind()
      data.claimDismissRating?.userError?.let { raise(ErrorMessage(it.message)) }
    }
  }
}
