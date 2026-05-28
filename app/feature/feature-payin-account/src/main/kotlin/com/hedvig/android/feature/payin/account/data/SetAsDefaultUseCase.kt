package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.safeExecuteAllowingPartialResponses
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.SetAsDefaultPayinMutation
import octopus.type.MemberPaymentProvider

internal interface SetAsDefaultUseCase {
  suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, PayinAccountData>
}

internal class SetAsDefaultUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
) : SetAsDefaultUseCase {
  override suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, PayinAccountData> {
    return either {
      apolloClient
        .mutation(SetAsDefaultPayinMutation(provider))
        .safeExecuteAllowingPartialResponses()
        .fold(
          fa = { error ->
            logcat { "SetAsDefaultUseCaseImpl error: $error" }
            raise(ErrorMessage())
          },
          fb = { result ->
            val userError = result.paymentMethodSetDefaultPayin?.message
            if (userError != null) {
              logcat { "SetAsDefaultUseCaseImpl userError not null: $userError" }
              raise(ErrorMessage(userError))
            }
            getPayinAccountUseCase.invoke().bind()
          },
          fab = {errors, _ ->
            logcat { "SetAsDefaultUseCaseImpl data with errors: $errors" }
            raise(ErrorMessage())
          }
        )
    }
  }
}
