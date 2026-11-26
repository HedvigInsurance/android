package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.YearInReviewQuery

internal interface GetYearInReviewUseCase {
  /** The member's year in review, or an error when it cannot be fetched or has no stories. */
  suspend fun invoke(fetchPolicy: FetchPolicy = FetchPolicy.NetworkFirst): Either<ErrorMessage, YearInReview>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetYearInReviewUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetYearInReviewUseCase {
  override suspend fun invoke(fetchPolicy: FetchPolicy): Either<ErrorMessage, YearInReview> = either {
    val data = apolloClient.query(YearInReviewQuery())
      .fetchPolicy(fetchPolicy)
      .safeExecute()
      .onLeft { error -> logcat(operationError = error) { "YearInReviewQuery failed with $error" } }
      .mapLeft { ErrorMessage() }
      .bind()
    val stories = data.currentMember.yearInReview.mapIndexed { index, article ->
      YearInReviewStory(
        id = index.toString(),
        title = article.title,
        description = article.description,
        imageUrl = article.fileUrl,
        action = article.action?.let { YearInReviewAction(title = it.title, url = it.url) },
      )
    }
    ensure(stories.isNotEmpty()) { ErrorMessage() }
    YearInReview(stories)
  }
}
