package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.RemovePayinMethodMutation
import octopus.type.MemberPaymentProvider

internal interface RemoveMethodUseCase {
  suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class RemoveMethodUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : RemoveMethodUseCase {
  override suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, Unit> = either {
    val response = apolloClient
      .mutation(RemovePayinMethodMutation(MemberPaymentProvider.safeValueOf(provider.rawValue)))
      .safeExecute()
    // The removed method is still in every cached payment query until the next fetch. Cleared on failure too,
    // since the backend may have removed it even when the response carries errors.
    networkCacheManager.clearCache()
    val result = response
      .mapLeft { error ->
        logcat(LogPriority.ERROR, error) { "RemovePayinMethodMutation error: $error" }
        ErrorMessage()
      }.bind()
    val userError = result.paymentMethodRemoveMethod?.message
    if (userError != null) {
      logcat(LogPriority.WARN) { "RemovePayinMethodMutation user error: $userError" }
      raise(ErrorMessage(userError))
    }
  }
}
