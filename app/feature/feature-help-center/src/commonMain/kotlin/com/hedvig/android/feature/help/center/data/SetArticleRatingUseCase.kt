package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.PuppyGuideEngagementMutation

interface SetArticleRatingUseCase {
  suspend fun invoke(articleName: String, rating: Int): Either<ErrorMessage, PuppyGuideEngagementMutation.Data>
}

internal class SetArticleRatingUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SetArticleRatingUseCase {
  override suspend fun invoke(
    articleName: String,
    rating: Int,
  ): Either<ErrorMessage, PuppyGuideEngagementMutation.Data> {
    return apolloClient
      .mutation(
        PuppyGuideEngagementMutation(
          name = articleName,
          rating = Optional.present(rating),
        ),
      )
      .safeExecute()
      .mapLeft { _ -> ErrorMessage() }
      .onRight { data ->
        logcat { "Mariia. Rating $rating for story $articleName set successfully" }
      }
  }
}
