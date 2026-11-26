package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.SetAsDefaultPayinMutation
import octopus.type.MemberPaymentProvider

internal interface SetAsDefaultUseCase {
  suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, PayinAccountData>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SetAsDefaultUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
) : SetAsDefaultUseCase {
  override suspend fun invoke(provider: PaymentProvider): Either<ErrorMessage, PayinAccountData> {
    return either {
      val result = apolloClient
        .mutation(SetAsDefaultPayinMutation(MemberPaymentProvider.safeValueOf(provider.rawValue)))
        .safeExecute()
        .mapLeft { error ->
          logcat(LogPriority.ERROR, error) { "SetAsDefaultPayinMutation error: $error" }
          ErrorMessage()
        }.bind()
      val userError = result.paymentMethodSetDefaultPayin?.message
      if (userError != null) {
        logcat(LogPriority.WARN) { "SetAsDefaultPayinMutation user error: $userError" }
        raise(ErrorMessage(userError))
      }
      getPayinAccountUseCase.invoke().bind()
    }
  }
}
