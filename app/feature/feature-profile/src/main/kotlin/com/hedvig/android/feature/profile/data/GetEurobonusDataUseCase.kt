package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import octopus.EurobonusDataQuery

internal interface GetEurobonusDataUseCase {
  suspend fun invoke(): Either<OperationResult.Error, EurobonusDataQuery.Data>
}

internal class GetEurobonusDataUseCaseImpl(private val apolloClient: ApolloClient) : GetEurobonusDataUseCase {
  override suspend fun invoke(): Either<OperationResult.Error, EurobonusDataQuery.Data> {
    return apolloClient.query(EurobonusDataQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither()
  }
}
