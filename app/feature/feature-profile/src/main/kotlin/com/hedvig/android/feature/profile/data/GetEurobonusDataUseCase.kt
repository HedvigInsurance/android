package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import octopus.EurobonusDataQuery

internal interface GetEurobonusDataUseCase {
  suspend fun invoke(): Either<ApolloOperationError, EurobonusDataQuery.Data>
}

internal class GetEurobonusDataUseCaseImpl(private val apolloClient: ApolloClient) : GetEurobonusDataUseCase {
  override suspend fun invoke(): Either<ApolloOperationError, EurobonusDataQuery.Data> {
    return apolloClient.query(EurobonusDataQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
  }
}
