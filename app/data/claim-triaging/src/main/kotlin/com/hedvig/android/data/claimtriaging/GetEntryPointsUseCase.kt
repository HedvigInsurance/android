package com.hedvig.android.data.claimtriaging

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import octopus.EntrypointSearchQuery

class GetEntryPointsUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(claimGroupId: ClaimGroupId?): Either<ErrorMessage, ImmutableList<EntryPoint>> {
    return either {
      val data = apolloClient
        .query(EntrypointSearchQuery(claimGroupId?.id))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      data.entrypointSearch.map { entrypointSearchItem: EntrypointSearchQuery.Data.EntrypointSearch ->
        EntryPoint(
          id = EntryPointId(entrypointSearchItem.id),
          displayName = entrypointSearchItem.displayName,
          entryPointOptions = entrypointSearchItem.options?.toEntryPointOptions(),
        )
      }.toImmutableList()
    }
  }
}
