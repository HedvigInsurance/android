package com.hedvig.android.feature.claimtriaging

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.toEntryPointOptions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import octopus.EntryPointGroupsQuery

internal class GetEntryPointGroupsUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, ImmutableList<ClaimGroup>> {
    return either {
      val data = apolloClient
        .query(EntryPointGroupsQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()

      data.entrypointGroups.map { entryPointGroup: EntryPointGroupsQuery.Data.EntrypointGroup ->
        ClaimGroup(
          ClaimGroupId(entryPointGroup.id),
          entryPointGroup.displayName,
          entryPointGroup.entrypoints.toEntryPoints(),
        )
      }.toImmutableList()
    }
  }
}

private fun List<octopus.fragment.EntryPoint>.toEntryPoints(): ImmutableList<EntryPoint> {
  return map { entryPoint ->
    EntryPoint(
      EntryPointId(entryPoint.id),
      entryPoint.displayName,
      entryPoint.options?.toEntryPointOptions(),
    )
  }.toImmutableList()
}
