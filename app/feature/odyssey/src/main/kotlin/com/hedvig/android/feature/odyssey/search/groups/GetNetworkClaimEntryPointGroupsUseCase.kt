package com.hedvig.android.feature.odyssey.search.groups

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.EntrypointGroupsQuery

internal class GetNetworkClaimEntryPointGroupsUseCase(
  private val apolloClient: ApolloClient,
) {

  suspend fun invoke(): Either<ErrorMessage, GroupedClaimsResult> {
    val query = EntrypointGroupsQuery()

    return either {
      val data = apolloClient.query(query).safeExecute()
        .toEither(::ErrorMessage)
        .bind()

      GroupedClaimsResult(
        memberName = data.currentMember.firstName,
        claimGroups = data.entrypointGroups.map { it.toClaimGroup() },
      )
    }
  }
}

private fun EntrypointGroupsQuery.Data.EntrypointGroup.toClaimGroup() = ClaimGroup(
  id = id,
  displayName = displayName,
  iconUrl = iconUrl,
)

data class GroupedClaimsResult(
  val memberName: String,
  val claimGroups: List<ClaimGroup>,
)
