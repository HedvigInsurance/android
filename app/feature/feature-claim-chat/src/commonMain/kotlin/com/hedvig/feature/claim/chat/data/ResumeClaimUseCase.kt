package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.ResumeClaimQuery

@SingleIn(AppScope::class)
@Inject
internal class ResumeClaimUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(): Either<ClaimChatErrorMessage, ClaimIntent?> {
    return either {
      apolloClient
        .query(
          ResumeClaimQuery(),
        )
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft { error ->
          logcat(operationError = error) { "ResumeClaimUseCase failed with $error" }
          ClaimChatErrorMessage.GeneralError
        }
        .bind()
        .currentMember.resumableClaimIntent
        ?.toClaimIntent(languageService.getLocale())
    }
  }
}
