package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.PuppyGuideEngagementMutation

internal interface SetArticleReadUseCase {
  suspend fun invoke(articleName: String): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SetArticleReadUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SetArticleReadUseCase {
  override suspend fun invoke(articleName: String): Either<ErrorMessage, Unit> {
    return either {
      val data = apolloClient
        .mutation(
          PuppyGuideEngagementMutation(
            name = articleName,
            read = Optional.present(true),
          ),
        )
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
      ensure(data.puppyGuideEngagement.success) { ErrorMessage() }
    }
      .onLeft { logcat { "setArticleReadUseCase failed!" } }
      .onRight { logcat { "setArticleReadUseCase set!" } }
  }
}
