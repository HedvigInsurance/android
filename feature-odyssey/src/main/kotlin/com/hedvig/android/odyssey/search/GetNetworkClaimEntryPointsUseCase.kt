package com.hedvig.android.odyssey.search

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import octopus.EntrypointSearchQuery

internal class GetNetworkClaimEntryPointsUseCase(
  private val apolloClient: ApolloClient,
) : GetClaimEntryPointsUseCase {

  override suspend fun invoke(): Either<ErrorMessage, CommonClaimsResult> {
    val query = EntrypointSearchQuery()

    return either {
      val data = apolloClient.query(query).safeExecute()
        .toEither(::ErrorMessage)
        .bind()

      val searchableClaims = data.entrypointSearch.toSearchableClaims()
      CommonClaimsResult(searchableClaims)
    }
  }
}

private fun List<EntrypointSearchQuery.Data.EntrypointSearch>.toSearchableClaims() = map {
  SearchableClaim(
    entryPointId = it.id,
    displayName = it.displayName,
    icon = null,
    itemType = ItemType(""),
    itemProblem = ItemProblem(""),
  )
}
