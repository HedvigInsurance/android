package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import octopus.UpdateEurobonusNumberMutation

internal interface UpdateEurobonusNumberUseCase {
  suspend fun invoke(newValueToSubmit: String): Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data>
}

internal class UpdateEurobonusNumberUseCaseImpl(private val apolloClient: ApolloClient) : UpdateEurobonusNumberUseCase {
  override suspend fun invoke(
    newValueToSubmit: String,
  ): Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data> {
    return apolloClient
      .mutation(UpdateEurobonusNumberMutation(newValueToSubmit))
      .safeExecute()
  }
}
