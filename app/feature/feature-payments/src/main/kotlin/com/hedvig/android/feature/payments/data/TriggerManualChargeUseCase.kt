package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.apollo.ErrorMessage
import kotlinx.datetime.LocalDate
import octopus.ManuallyChargeMemberMutation

internal interface TriggerManualChargeUseCase {
  suspend fun invoke(): Either<ErrorMessage, Unit>
}

internal class TriggerManualChargeUseCaseImpl(
  private val apolloClient: ApolloClient
): TriggerManualChargeUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Unit>  = either {
    val result = apolloClient
      .mutation(ManuallyChargeMemberMutation())
      .safeExecute()
      .mapLeft { raise(ErrorMessage()) }
      .bind()

    if (result.manuallyChargeMember.userError!=null) raise(ErrorMessage(
      result.manuallyChargeMember.userError.message
    )) else Unit
  }
}
