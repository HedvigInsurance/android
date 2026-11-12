package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.UpdateEurobonusNumberMutation

internal interface UpdateEurobonusNumberUseCase {
  suspend fun invoke(newValueToSubmit: String): Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class UpdateEurobonusNumberUseCaseImpl(private val apolloClient: ApolloClient) : UpdateEurobonusNumberUseCase {
  override suspend fun invoke(
    newValueToSubmit: String,
  ): Either<ApolloOperationError, UpdateEurobonusNumberMutation.Data> {
    return apolloClient
      .mutation(UpdateEurobonusNumberMutation(newValueToSubmit))
      .safeExecute()
  }
}
