package com.hedvig.android.odyssey.search

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeArrayRestCall
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import octopus.EntrypointSearchQuery
import octopus.type.EntrypointSearchInput
import octopus.type.EntrypointType
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

internal class GetNetworkClaimEntryPointsUseCase(
  private val apolloClient: ApolloClient,
) : GetClaimEntryPointsUseCase {

  override suspend operator fun invoke(): Either<ErrorMessage, CommonClaimsResult> {
    val query = EntrypointSearchQuery(
      input = EntrypointSearchInput(
        type = EntrypointType.CLAIM,
        limit = Optional.present(NR_OF_ENTRYPOINTS),
      ),
    )

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

private const val NR_OF_ENTRYPOINTS = 20

@Serializable
internal data class ClaimEntryPointDTO(
  val id: String,
  val displayName: String,
)
