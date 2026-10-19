package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.SetAsDefaultPayoutMutation
import octopus.type.MemberPaymentProvider

@SingleIn(AppScope::class)
@Inject
internal class SetAsDefaultPayoutUseCase(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) {
  suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, Unit> = either {
    val result = apolloClient
      .mutation(SetAsDefaultPayoutMutation(MemberPaymentProvider.safeValueOf(provider.rawValue)))
      .safeExecute()
      .mapLeft { error ->
        logcat(LogPriority.ERROR, error) { "SetAsDefaultPayoutMutation error: $error" }
        ErrorMessage()
      }.bind()
    val userError = result.paymentMethodSetDefaultPayout?.message
    if (userError != null) {
      logcat(LogPriority.WARN) { "SetAsDefaultPayoutMutation user error: $userError" }
      raise(ErrorMessage(userError))
    }
    networkCacheManager.clearCache()
  }
}
