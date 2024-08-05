package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import octopus.UpdateEurobonusNumberMutation

internal interface UpdateEurobonusNumberUseCase {
  suspend fun invoke(newValueToSubmit: String): Either<OperationResult.Error, UpdateEurobonusNumberMutation.Data>
}

internal class UpdateEurobonusNumberUseCaseImpl(private val apolloClient: ApolloClient) : UpdateEurobonusNumberUseCase {
  override suspend fun invoke(
    newValueToSubmit: String,
  ): Either<OperationResult.Error, UpdateEurobonusNumberMutation.Data> {
    return apolloClient.mutation(UpdateEurobonusNumberMutation(newValueToSubmit))
      .safeExecute()
      .toEither()
  }
}
