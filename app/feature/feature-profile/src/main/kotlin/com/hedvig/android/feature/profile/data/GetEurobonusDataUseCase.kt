package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.EurobonusDataQuery

internal interface GetEurobonusDataUseCase {
  suspend fun invoke(): Either<ApolloOperationError, EurobonusDataQuery.Data>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetEurobonusDataUseCaseImpl(private val apolloClient: ApolloClient) : GetEurobonusDataUseCase {
  override suspend fun invoke(): Either<ApolloOperationError, EurobonusDataQuery.Data> {
    return apolloClient.query(EurobonusDataQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
  }
}
