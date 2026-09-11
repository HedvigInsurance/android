package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecuteAllowingPartialResponses
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.RemovePayinMethodMutation
import octopus.type.MemberPaymentProvider

internal interface RemoveMethodUseCase {
  suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class RemoveMethodUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : RemoveMethodUseCase {
  override suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, Unit> = either {
    apolloClient
      .mutation(RemovePayinMethodMutation(provider))
      .safeExecuteAllowingPartialResponses()
      .fold(
        fa = { error ->
          logcat(LogPriority.ERROR) { "RemovePayinMethodMutation error: $error" }
          raise(ErrorMessage())
        },
        fb = { result ->
          val userError = result.paymentMethodRemoveMethod?.message
          if (userError != null) {
            logcat(LogPriority.ERROR) { "RemovePayinMethodMutation user error: $userError" }
            raise(ErrorMessage(userError))
          }
          // The removed method is still in every cached payment query until the next fetch.
          networkCacheManager.clearCache()
        },
        fab = { errors, _ ->
          logcat(LogPriority.ERROR) { "RemovePayinMethodMutation data with errors: $errors" }
          raise(ErrorMessage())
        },
      )
  }
}
