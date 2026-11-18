package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.GreenCardsQuery

internal interface GetGreenCardsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<GreenCard>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetGreenCardsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetGreenCardsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<GreenCard>> {
    return apolloClient.query(GreenCardsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .onLeft { logcat(operationError = it) { "GetGreenCardsUseCase: $it" } }
      .mapLeft(::ErrorMessage)
      .map { data ->
        val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        data.currentMember.greenCards.map {
          GreenCard(
            id = it.id,
            signedUrl = it.signedUrl,
            registrationNumber = it.registrationNumber,
            validFrom = it.validFrom,
            validTo = it.validTo,
            isExpiredNow = it.validTo < today,
          )
        }
      }
  }
}
