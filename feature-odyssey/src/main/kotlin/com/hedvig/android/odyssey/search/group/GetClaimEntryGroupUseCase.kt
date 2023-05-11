package com.hedvig.android.odyssey.search.group

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.odyssey.model.ItemProblem
import com.hedvig.android.feature.odyssey.model.ItemType
import com.hedvig.android.odyssey.search.commonclaims.SearchableClaim
import octopus.EntrypointSearchQuery

internal class GetClaimEntryGroupUseCase(
  private val apolloClient: ApolloClient,
) {

  suspend fun invoke(groupId: String): Either<ErrorMessage, CommonClaimsResult> {
    val query = EntrypointSearchQuery(groupId)

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

data class CommonClaimsResult(val searchableClaims: List<SearchableClaim>)
