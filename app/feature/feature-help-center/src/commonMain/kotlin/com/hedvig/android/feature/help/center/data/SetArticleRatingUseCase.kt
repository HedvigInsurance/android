package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.PuppyGuideEngagementMutation

interface SetArticleRatingUseCase {
  suspend fun invoke(articleName: String, rating: Int): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SetArticleRatingUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SetArticleRatingUseCase {
  override suspend fun invoke(articleName: String, rating: Int): Either<ErrorMessage, Unit> {
    return either {
      val data = apolloClient
        .mutation(
          PuppyGuideEngagementMutation(
            name = articleName,
            rating = Optional.present(rating),
          ),
        )
        .safeExecute()
        .mapLeft { _ -> ErrorMessage() }
        .bind()
      ensure(data.puppyGuideEngagement.success) { ErrorMessage() }
    }
  }
}
